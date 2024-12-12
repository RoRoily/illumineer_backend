package com.buaa01.illumineer_backend.service.impl.paper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.mapper.SearchResultPaperMapper;
import com.buaa01.illumineer_backend.mapper.StormMapper;
import com.buaa01.illumineer_backend.service.paper.PaperSearchService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.buaa01.illumineer_backend.utils.PaperSortScorer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class PaperSearchServiceImpl implements PaperSearchService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private SearchResultPaperMapper searchResultPaperMapper;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据pid获取文献信息
     * 
     * @param pid 文献ID
     * @return Paper
     */
    @Override
    public CustomResponse getPaperByPid(Long pid) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> paper = paperMapper.getPaperByPid(pid);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // keywords 的转换
            List<String> keywords = objectMapper.readValue(paper.get("keywords").toString(), new TypeReference<List<String>>() {});
            paper.put("keywords", keywords);

            // auths 的转换
            Map<String, Integer> auths = objectMapper.readValue(paper.get("auths").toString(), new TypeReference<Map<String, Integer>>() {});
            paper.put("auths", auths);

            // refs 的转换
            List<Long> refs = objectMapper.readValue(paper.get("refs").toString(), new TypeReference<List<Long>>() {});
            paper.put("refs", refs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        customResponse.setData(paper);
        return customResponse;
    }

    /**
     * @description: 根据stats返回相应的Paper
     * @param: [stats 状态, size 一页的条数, offset 第几页, sortType 排序依据, order 升序/降序]
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order    0=降序，1=升序
     * @return: Paper
     **/
    @Override
    public CustomResponse getPaperByStats(Integer stats, Integer size, Integer offset, Integer sortType,
            Integer order) {
        CustomResponse customResponse = new CustomResponse();
        int total = 0;

        List<Map<String, Object>> papers = paperMapper.getPapersByStats(stats);
        total = papers.size();

        // 1. 将 Paper 转换成 SearchReultPaper 类型
        List<SearchResultPaper> searchResultPapers = papersToSearchResultPaper(papers);

        // 2. searchbByOrder 对搜索结果进行排序：sortType
        searchResultPapers = searchByOrder(searchResultPapers, sortType, order);

        // 3. searchByPage 对排序结果进行分页，并将当前页 offset 需要的内容返回
        searchResultPapers = searchByPage(searchResultPapers, size, offset);

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("result", searchResultPapers);
        result.put("total", total);
        customResponse.setData(result);
        return customResponse;
    }

    /**
     * 一框式检索：搜索文献（分页、排序）
     * 
     * @param condition 筛选条件（选择查找的字段）
     * @param keyword   搜索内容
     * @param size      一页多少条内容
     * @param offset    第几页
     * @param sortType  根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order     0=降序，1=升序
     * @return SearchResultPaper
     */
    @Override
    public CustomResponse searchPapers(String condition, String keyword, Integer size, Integer offset, Integer sortType,
            Integer order) {
        // 模糊搜索：keyword
        List<Map<String, Object>> papers = searchByKeyword(condition, keyword);

        List<SearchResultPaper> searchResultPapers = papersToSearchResultPaper(papers);

        deleteFromRedis(); // 清空上次缓存的搜索结果
        saveToRedis(searchResultPapers); // 存储新的搜索结果

        return getSearchResult(searchResultPapers, sortType, order, size, offset);
    }

    /**
     * 高级检索
     * 
     * @param logic none=0/and=1/or=2/not=3
     * @param condition
     * @param keyword（传 name 或者 %name%）
     * @param size       一页多少条内容
     * @param offset     第几页
     * @param sortType   根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order      0=降序，1=升序
     * @return SearchResultPaper
     */
    @Override
    public CustomResponse advancedSearchPapers(List<Integer> logic, List<String> condition, List<String> keyword, Integer size, Integer offset,
            Integer sortType, Integer order) {
        Set<Long> paper1 = new HashSet<>();
        Set<Long> paper2 = new HashSet<>();

        int n = condition.size();
        for (int i=0; i<n; i++) {
            // 对该查询条件进行查询
            QueryWrapper<Paper> queryWrapper = Wrappers.query();
            if (logic.get(i) == 3 || logic.get(i) < 0) { // NOT
                queryWrapper.eq("stats", 0);
                queryWrapper.notLike(condition.get(i), keyword.get(i));
            } else {
                queryWrapper.eq("stats", 0);
                queryWrapper.like(condition.get(i), keyword.get(i));
            }
            List<Paper> papers = paperMapper.selectList(queryWrapper);
            if (paper1.isEmpty()) {
                paper1 = new HashSet<>();
                for (Paper paper: papers) {
                    paper1.add(paper.getPid());
                }

            } else {
                paper2 = new HashSet<>();
                for (Paper paper: papers) {
                    paper2.add(paper.getPid());
                }
            }

            // 进行集合运算
            if (logic.get(i) == 1) { // AND
                paper1.retainAll(paper2);
            } else if (logic.get(i) == 2) { // OR
                paper1.addAll(paper2);
            }
            paper2.clear();
        }
        List<Map<String, Object>> paperList = new ArrayList<Map<String, Object>>();
        for (Long pid: paper1) {
            Map<String, Object> paper = paperMapper.getPaperByPid(pid);
            paperList.add(paper);
        }
        List<SearchResultPaper> searchResultPapers = papersToSearchResultPaper(paperList);

        deleteFromRedis();
        saveToRedis(searchResultPapers);

        return getSearchResult(searchResultPapers, sortType, order, size, offset);
    }

    /*
     * ========== 相关方法 ==========
     */

    private List<SearchResultPaper> papersToSearchResultPaper(List<Map<String, Object>> papers) {
        List<SearchResultPaper> searchResultPapers = new ArrayList<>();

        for (Map<String, Object> paper : papers) {
            Date date;
            // 判断是否是 ISO 格式，转换date格式
            if (!paper.get("publish_date").toString().contains(" ")) {
                date = Date.from(LocalDateTime.parse(paper.get("publish_date").toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atZone(ZoneId.systemDefault()).toInstant());
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                date = Date.from(LocalDateTime.parse(paper.get("publish_date").toString(), formatter)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }

            SearchResultPaper searchResultPaper = new SearchResultPaper(
                    Long.parseLong(paper.get("pid").toString()),
                    paper.get("title").toString(),
                    paper.get("keywords")==null?"":paper.get("keywords").toString(),
                    paper.get("auths")==null?"":paper.get("auths").toString(),
                    paper.get("field").toString(),
                    paper.get("type").toString(),
                    paper.get("theme").toString(),
                    date,
                    paper.get("derivation").toString(),
                    Integer.parseInt(paper.get("ref_times").toString()),
                    Integer.parseInt(paper.get("fav_time").toString()),
                    paper.get("content_url").toString());
            searchResultPapers.add(searchResultPaper);
        }

        return searchResultPapers;
    }

    // 获取返回的结果：包括搜索结果 SearchResultPaper，过滤用的选项字段 option
    private CustomResponse getSearchResult(List<SearchResultPaper> papers, Integer sortType, Integer order,
            Integer size, Integer offset) {
        Map<String, Object> result = new HashMap<>();
        int total = papers.size();

        // 1. 根据搜索结果获取筛选字段的选择项
        Map<String, Map<String, Integer>> options = getOptions(papers);

        // 2. searchbByOrder 对搜索结果进行排序：sortType
        papers = searchByOrder(papers, sortType, order);

        // 3. searchByPage 对排序结果进行分页，并将当前页 offset 需要的内容返回
        papers = searchByPage(papers, size, offset);

        // 4. 返回结果
        CustomResponse customResponse = new CustomResponse();
        result.put("result", papers); // 搜索结果
        result.put("options", options); // 年份、来源、类型、主题
        result.put("total", total); // 总数

        customResponse.setData(result);
        return customResponse;
    }

    // 获取筛选字段的选项
    Map<String, Map<String, Integer>> getOptions(List<SearchResultPaper> papers) {
        Map<String, Integer> years = new LinkedHashMap<>();
        Map<String, Integer> derivations = new LinkedHashMap<>();
        Map<String, Integer> types = new LinkedHashMap<>();
        Map<String, Integer> themes = new LinkedHashMap<>();

        for (SearchResultPaper paper : papers) {
            System.out.println(paper.getPublishDate());
            String year;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            if (paper.getPublishDate().toString().contains(" ")) {
                year = ZonedDateTime.parse(paper.getPublishDate().toString(), formatter).getYear() + "";
            } else {
                year = years.get(paper.getPublishDate().getYear()).toString();
            }
            // year
            System.out.println(year);
            if (years.get(year) == null) {
                years.put(year, 1);
            } else {
                years.put(year, years.get(year) + 1);
            }
            // derivations
            if (derivations.get(paper.getDerivation()) == null) {
                derivations.put(paper.getDerivation(), 1);
            } else {
                derivations.put(paper.getDerivation(), derivations.get(paper.getDerivation()) + 1);
            }
            // types
            if (types.get(paper.getType()) == null) {
                types.put(paper.getType(), 1);
            } else {
                types.put(paper.getType(), types.get(paper.getType()) + 1);
            }
            // themes
            if (themes.get(paper.getTheme()) == null) {
                themes.put(paper.getTheme(), 1);
            } else {
                themes.put(paper.getTheme(), themes.get(paper.getTheme()) + 1);
            }
        }

        // 按值大到小进行排序
        // 1. 将LinkedHashMap转换为List
        List<Map.Entry<String, Integer>> yearsList = new ArrayList<>(years.entrySet());
        List<Map.Entry<String, Integer>> derivationsList = new ArrayList<>(derivations.entrySet());
        List<Map.Entry<String, Integer>> typesList = new ArrayList<>(types.entrySet());
        List<Map.Entry<String, Integer>> themesList = new ArrayList<>(themes.entrySet());
        // 2. 使用 Comparator 对 List 进行排序
        yearsList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        derivationsList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        typesList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        themesList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<String, Map<String, Integer>> options = new HashMap<>();

        years = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> year: yearsList) {
            years.put(year.getKey(), year.getValue());
        }
        derivations = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> derivation: derivationsList) {
            derivations.put(derivation.getKey(), derivation.getValue());
        }
        types = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> type: typesList) {
            types.put(type.getKey(), type.getValue());
        }
        themes = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> theme: themesList) {
            themes.put(theme.getKey(), theme.getValue());
        }

        options.put("years", years);
        options.put("derivations", derivations);
        options.put("types", types);
        options.put("themes", themes);
        return options;
    }

    /**
     * 模糊查询
     * 
     * @param keyword 搜索内容
     * @return 文献信息
     */
    List<Map<String, Object>> searchByKeyword(String condition, String keyword) {
        List<Paper> list = null;
        try {
            Query query;

            // query = Query.of(q -> q.match(m -> m.field(condition).query(keyword)));
            query = Query.of(q -> q.bool(b -> {
                b.must(m -> m.match(ma -> ma.field(condition).query(keyword)));
                b.must(m -> m.match(ma -> ma.field("stats").query(0)));
                return b;
            }));

            SearchRequest searchRequest = new SearchRequest.Builder().index("paper").query(query).build();
            SearchResponse<Paper> searchResponse = client.search(searchRequest, Paper.class);
            list = new ArrayList<>();
            for (Hit<Paper> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source());
                }
            }
        } catch (IOException e) {
            log.error("查询ES相关文献文档时出错了：" + e);
            list = null;
        }
        List<Map<String, Object>> paperList;
        if (list == null) {
            paperList = paperMapper.searchByKeyword(condition, keyword);
        } else {
            paperList = new ArrayList<Map<String, Object>>();
            for (Paper paper: list) {
                Map<String, Object> paperMap = new HashMap<>();
                paperMap.put("pid", paper.getPid());
                paperMap.put("title", paper.getTitle());
                paperMap.put("keywords", paper.getKeywords());
                paperMap.put("auths", paper.getAuths());
                paperMap.put("field", paper.getField());
                paperMap.put("type", paper.getField());
                paperMap.put("theme", paper.getTheme());
                paperMap.put("publish_date", paper.getPublishDate());
                paperMap.put("derivation", paper.getDerivation());
                paperMap.put("ref_times", paper.getRefTimes());
                paperMap.put("fav_time", paper.getFavTime());
                paperList.add(paperMap);
            }
        }
        return paperList;
    }

    /**
     * 分页
     * 
     * @param papers  搜索的结果
     * @param pageNum 一页的条目数量
     * @param offset  第几页
     * @return 文献信息
     */
    List<SearchResultPaper> searchByPage(List<SearchResultPaper> papers, Integer pageNum, Integer offset) {
        if (offset == null || offset == 0) {
            offset = 1;
        }
        if (pageNum == null || pageNum == 0) {
            pageNum = 20;
        }
        int startIndex = (offset - 1) * pageNum;
        int endIndex = startIndex + pageNum;
        // 检查数据是否足够满足分页查询
        if (startIndex > papers.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return Collections.emptyList();
        }
        endIndex = Math.min(endIndex, papers.size());
        List<SearchResultPaper> sublist = papers.subList(startIndex, endIndex);
        if (sublist.isEmpty()) {
            return Collections.emptyList();
        }
        // List<Map<String, Object>> mapList = new ArrayList<>();
        // for(Paper paper : papers) {
        // Map<String, Object> map = getPaperMap(paper);
        // mapList.add(map);
        // }
        // return mapList;
        return sublist;
    }

    /**
     * 排序
     * 
     * @param papers   搜索的结果
     * @param sortType 根据这个来进行排序
     *                 <p>
     *                 1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数，4=评价分数
     * @param order    0=降序，1=升序
     * @return 文献信息
     */
    List<SearchResultPaper> searchByOrder(List<SearchResultPaper> papers, Integer sortType, Integer order) {
        // 降序
        if (order == 0) {
            papers.sort((p1, p2) -> {
                if (sortType == 1) {
                    return p2.getPublishDate().compareTo(p1.getPublishDate());
                } else if (sortType == 2) {
                    return p2.getRefTimes() - p1.getRefTimes();
                } else if (sortType == 3) {
                    return p2.getFavTime() - p1.getFavTime();
                }
                return 0;
            });
        }
        // 升序
        else if (order == 1) {
            papers.sort((p1, p2) -> {
                if (sortType == 1) {
                    return p1.getPublishDate().compareTo(p2.getPublishDate());
                } else if (sortType == 2) {
                    return p1.getRefTimes() - p2.getRefTimes();
                } else if (sortType == 3) {
                    return p1.getFavTime() - p2.getFavTime();
                }
                return 0;
            });
        }
        return papers;
    }

    /**
     * 对搜索结果进行排序,尝试使用评分制
     * 
     * @param papers   搜索结果
     * @param sortType 4=按评分排序
     * @param order    0=降序，1=升序
     * @param keyWords 关键词
     *                 <p>
     *                 以提供检索契合度评分
     * @return
     */
    List<SearchResultPaper> searchByOrder(List<SearchResultPaper> papers, Integer sortType, Integer order,
            List<String> keyWords) {
        if (sortType != 4) {
            papers = searchByOrder(papers, sortType, order);
        } else {
            Map<SearchResultPaper, Double> paperScore = new HashMap<>();
            papers.sort((p1, p2) -> {
                double s1 = paperScore.computeIfAbsent(p1, p -> PaperSortScorer.calculateScore(p1, keyWords));
                double s2 = paperScore.computeIfAbsent(p2, p -> PaperSortScorer.calculateScore(p2, keyWords));
                if (order == 0) {
                    return Double.compare(s2, s1);
                } else {
                    return Double.compare(s1, s2);
                }
            });
        }
        return papers;
    }

    /*
     * ========== 缓存操作 ==========
     */

    // 将查询结果存到 redis 中
    public void saveToRedis(List<SearchResultPaper> searchResultPapers) {
        for (SearchResultPaper paper : searchResultPapers) {
            CompletableFuture.runAsync(() -> {
                redisTool.setExObjectValue("paper" + paper.getPid(), paper); // 异步更新到redis
            }, taskExecutor);
        }
    }

    // 从 redis 中获取暂存信息
    public List<SearchResultPaper> getFromRedis() {
        List<SearchResultPaper> papers = new ArrayList<>();
        Set<String> keySet = redisTool.getKeysByPrefix("paper");
        for (String key : keySet) {
            SearchResultPaper paper = redisTool.getObjectByClass(key, SearchResultPaper.class);
            papers.add(paper);
        }
        return papers;
    }

    // 删除 redis 中的信息
    public void deleteFromRedis() {
        redisTool.deleteByPrefix("paper");
    }

}

package com.buaa01.illumineer_backend.service.impl.paper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Papers;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.mapper.SearchResultPaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperSearchService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.buaa01.illumineer_backend.utils.PaperSortScorer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
     * @return Papers
     */
    @Override
    public CustomResponse getPaperByPid(Integer pid) {
        CustomResponse customResponse = new CustomResponse();
        Papers paper = null;
        QueryWrapper<Papers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        paper = paperMapper.selectOne(queryWrapper);
        // paper = paperMapper.getPaperByPid(pid);

        // Map<String, Object> map = new HashMap<>();
        // map.put("title", paper.getTitle());
        // map.put("essAbs", paper.getEssAbs());
        // map.put("keywords", paper.getKeywords());
        // map.put("contentUrl", paper.getContentUrl());
        // map.put("auths", paper.getAuths());
        // map.put("field", paper.getCategory());
        // map.put("type", paper.getType());
        // map.put("theme", paper.getTheme());
        // map.put("publishDate", paper.getPublishDate());
        // map.put("derivation", paper.getDerivation());
        // map.put("ref_times", paper.getRef_times());
        // map.put("fav_times", paper.getFav_time());
        // map.put("refs", paper.getRefs());

        customResponse.setData(paper);
        return customResponse;
    }

    /**
     * @description: 根据stats返回相应的Paper
     * @param: [stats 状态, size 一页的条数, offset 第几页, sortType 排序依据, order 升序/降序]
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order    0=降序，1=升序
     * @return: Papers
     **/
    @Override
    public CustomResponse getPaperByStats(Integer stats, Integer size, Integer offset, Integer sortType,
            Integer order) {
        CustomResponse customResponse = new CustomResponse();

        List<Papers> papers = null;
        QueryWrapper<Papers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("stats", stats);
        papers = paperMapper.selectList(queryWrapper);

        // 1. 将 Papers 转换成 SearchReultPaper 类型
        List<SearchResultPaper> searchResultPapers = papersToSearchResultPaper(papers);

        // 2. searchbByOrder 对搜索结果进行排序：sortType
        searchResultPapers = searchByOrder(searchResultPapers, sortType, order);

        // 3. searchByPage 对排序结果进行分页，并将当前页 offset 需要的内容返回
        searchResultPapers = searchByPage(searchResultPapers, size, offset);

        // 4. 返回结果
        customResponse.setData(searchResultPapers);
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
        List<Papers> papers = null;
        papers = searchByKeyword(condition, keyword);

        List<SearchResultPaper> searchResultPapers = papersToSearchResultPaper(papers);

        deleteFromRedis(); // 清空上次缓存的搜索结果
        saveToRedis(searchResultPapers); // 存储新的搜索结果

        return getSearchResult(searchResultPapers, sortType, order, size, offset);
    }

    /**
     * 高级检索
     * 
     * @param conditions 条件：logic(none=0/and=1/or=2/not=3), condition, keyword（传
     *                   name 或者 %name%）
     * @param size       一页多少条内容
     * @param offset     第几页
     * @param sortType   根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order      0=降序，1=升序
     * @return SearchResultPaper
     */
    @Override
    public CustomResponse advancedSearchPapers(List<Map<String, String>> conditions, Integer size, Integer offset,
            Integer sortType, Integer order) {
        Set<Papers> papers1 = new HashSet<>();
        Set<Papers> papers2 = new HashSet<>();

        for (Map<String, String> condition : conditions) {
            // 对该查询条件进行查询
            QueryWrapper<Papers> queryWrapper = Wrappers.query();
            if (condition.get("condition").equals("publishYear")) {
                String year = condition.get("keyword");
                condition.put("keyword", year + "-%-%");
            }
            if (condition.get("logic").equals("3")) { // NOT
                queryWrapper.eq("stats", 0);
                queryWrapper.notLike(condition.get("condition"), condition.get("keyword"));
            } else {
                queryWrapper.eq("stats", 0);
                queryWrapper.like(condition.get("condition"), condition.get("keyword"));
            }
            List<Papers> papers = paperMapper.selectList(queryWrapper);
            if (papers1.isEmpty()) {
                papers1 = new HashSet<>(papers);
            } else {
                papers2 = new HashSet<>(papers);
            }

            // 进行集合运算
            if (condition.get("logic").equals("1")) { // AND
                papers1.retainAll(papers2);
            } else if (condition.get("logic").equals("2")) { // OR
                papers1.addAll(papers2);
            }
            papers2.clear();
        }

        List<Papers> papers = papers1.stream().toList();
        List<SearchResultPaper> searchResultPapers = papersToSearchResultPaper(papers);

        deleteFromRedis();
        saveToRedis(searchResultPapers);

        return getSearchResult(searchResultPapers, sortType, order, size, offset);
    }

    /*
     * ========== 相关方法 ==========
     */

    private List<SearchResultPaper> papersToSearchResultPaper(List<Papers> papers) {
        List<SearchResultPaper> searchResultPapers = new ArrayList<>();

        for (Papers paper : papers) {
            SearchResultPaper searchResultPaper = new SearchResultPaper(
                    paper.getPid(),
                    paper.getTitle(),
                    paper.getKeywords(),
                    paper.getAuths(),
                    paper.getField(),
                    paper.getType(),
                    paper.getTheme(),
                    paper.getPublishDate(),
                    paper.getDerivation(),
                    paper.getRef_times(),
                    paper.getFav_time());
            searchResultPapers.add(searchResultPaper);
        }

        return searchResultPapers;
    }

    // 获取返回的结果：包括搜索结果 SearchResultPaper，过滤用的选项字段 option
    private CustomResponse getSearchResult(List<SearchResultPaper> papers, Integer sortType, Integer order,
            Integer size, Integer offset) {
        Map<String, Object> result = new HashMap<>();

        List<Map.Entry<String, Integer>> years = null;
        List<Map.Entry<String, Integer>> derivations = null;
        List<Map.Entry<String, Integer>> types = null;
        List<Map.Entry<String, Integer>> themes = null;

        // 1. 根据搜索结果获取筛选字段的选择项
        Map<String, List<Map.Entry<String, Integer>>> options = getOptions(papers);
        years = options.get("years");
        derivations = options.get("derivations");
        types = options.get("types");
        themes = options.get("themes");

        // 2. searchbByOrder 对搜索结果进行排序：sortType
        papers = searchByOrder(papers, sortType, order);

        // 3. searchByPage 对排序结果进行分页，并将当前页 offset 需要的内容返回
        papers = searchByPage(papers, size, offset);

        // 4. 返回结果
        CustomResponse customResponse = new CustomResponse();
        result.put("result", papers); // 搜索结果
        result.put("year", years); // 年份（从出版时间publishDate解析）
        result.put("derivation", derivations); // 来源
        result.put("type", types); // 类型
        result.put("theme", themes); // 主题

        customResponse.setData(result);
        return customResponse;
    }

    // 获取筛选字段的选项
    Map<String, List<Map.Entry<String, Integer>>> getOptions(List<SearchResultPaper> papers) {
        Map<String, Integer> years = new LinkedHashMap<>();
        Map<String, Integer> derivations = new LinkedHashMap<>();
        Map<String, Integer> types = new LinkedHashMap<>();
        Map<String, Integer> themes = new LinkedHashMap<>();

        for (SearchResultPaper paper : papers) {
            // year
            if (years.get(paper.getPublishDate().getYear() + "") == null) {
                years.put(paper.getPublishDate().getYear() + "", 0);
            } else {
                years.put(paper.getPublishDate().getYear() + "", years.get(paper.getPublishDate().getYear() + "") + 1);
            }
            // derivations
            if (derivations.get(paper.getDerivation()) == null) {
                derivations.put(paper.getDerivation(), 0);
            } else {
                derivations.put(paper.getDerivation(), derivations.get(paper.getDerivation()) + 1);
            }
            // types
            if (types.get(paper.getType()) == null) {
                types.put(paper.getType(), 0);
            } else {
                types.put(paper.getType(), types.get(paper.getType()) + 1);
            }
            // themes
            if (themes.get(paper.getTheme()) == null) {
                themes.put(paper.getTheme(), 0);
            } else {
                themes.put(paper.getTheme(), themes.get(paper.getTheme()) + 1);
            }
        }

        // 按值大到小进行排序
        // 1. 将LinkedHashMap转换为List
        List<Map.Entry<String, Integer>> yearsList = new ArrayList<>(years.entrySet());
        List<Map.Entry<String, Integer>> derivationsList = new ArrayList<>(years.entrySet());
        List<Map.Entry<String, Integer>> typesList = new ArrayList<>(years.entrySet());
        List<Map.Entry<String, Integer>> themesList = new ArrayList<>(years.entrySet());
        // 2. 使用 Comparator 对 List 进行排序
        yearsList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        derivationsList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        typesList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        themesList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<String, List<Map.Entry<String, Integer>>> options = new HashMap<>();
        options.put("years", yearsList);
        options.put("derivations", derivationsList);
        options.put("types", typesList);
        options.put("themes", themesList);
        return options;
    }

    /**
     * 模糊查询
     * 
     * @param keyword 搜索内容
     * @return 文献信息
     */
    List<Papers> searchByKeyword(String condition, String keyword) {
        try {
            List<Papers> list = new ArrayList<>();
            Query query;
            if (condition.equals("publishYear")) {
                // query = Query.of(q -> q.match(m -> m.field(condition).query(keyword
                // +"-%-%")));
                query = Query.of(q -> q.bool(b -> {
                    b.must(m -> m.match(ma -> ma.field(condition).query(keyword + "-%-%")));
                    b.must(m -> m.match(ma -> ma.field("stats").query(0)));
                    // b.filter(f -> f.term(t -> t.field("stats").value(0)));
                    // TODO: 是否可以改为filter来实现，以优化性能
                    return b;
                }));
            } else {
                // query = Query.of(q -> q.match(m -> m.field(condition).query(keyword)));
                query = Query.of(q -> q.bool(b -> {
                    b.must(m -> m.match(ma -> ma.field(condition).query(keyword)));
                    b.must(m -> m.match(ma -> ma.field("stats").query(0)));
                    return b;
                }));
            }
            SearchRequest searchRequest = new SearchRequest.Builder().index("paper").query(query).build();
            SearchResponse<Papers> searchResponse = client.search(searchRequest, Papers.class);
            for (Hit<Papers> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source());
                }
            }
            return list;
        } catch (IOException e) {
            log.error("查询ES相关文献文档时出错了：" + e);
            return Collections.emptyList();
        }
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
        // for(Papers paper : papers) {
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
                    return p2.getRef_times() - p1.getRef_times();
                } else if (sortType == 3) {
                    return p2.getFav_time() - p1.getFav_time();
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
                    return p1.getRef_times() - p2.getRef_times();
                } else if (sortType == 3) {
                    return p1.getFav_time() - p2.getFav_time();
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

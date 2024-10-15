package com.buaa01.illumineer_backend.service.impl.paper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

@Slf4j
public class PaperServiceImpl implements PaperService {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private PaperMapper paperMapper;

    @Override
    public CustomResponse getPaperByPid(Integer pid) {
        CustomResponse customResponse = new CustomResponse();
        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
//        paper = paperMapper.selectOne(queryWrapper);
        paper = paperMapper.getPaperByPid(pid);

        // 文献引用格式
        List<String> references = generateRef(paper.getAuths(), paper.getTitle(), paper.getDerivation());

        Map<String, Object> map = new HashMap<>();
        map.put("title",paper.getTitle());
        map.put("essAbs", paper.getEssAbs());
        map.put("keywords",paper.getKeywords());
        map.put("contentUrl",paper.getContentUrl());
        map.put("auths",paper.getAuths());
        map.put("field",paper.getField());
        map.put("type",paper.getType());
        map.put("theme",paper.getTheme());
        map.put("publishDate",paper.getPublishDate());
        map.put("derivation",paper.getDerivation());
        map.put("ref_times", paper.getRef_times());
        map.put("fav_times",paper.getFav_time());
        map.put("refs",paper.getRefs());
        customResponse.setData(map);
        return customResponse;
    }

    // 一框式检索：模糊查询 + 排序 + 分页
    @Override
    public CustomResponse searchPapers(String keyword, Integer offset, Integer sortType) {
        List<Paper> papers = null;

        // 1. 模糊搜索：keyword
        papers = searchByKeyword(keyword);

        // 2. searchbByOrder 对搜索结果进行排序：sortType
        papers = searchByOrder(papers, sortType);

        // 3. searchByPage 对排序结果进行分页，并将当前页 offset 需要的内容返回
        papers = searchByPage(papers, 20, offset);

        // 4. 返回结果
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(papers);
        return customResponse;
    }

    /**
     * 模糊查询
     * @param keyword 搜索内容
     * @return 文献信息
     */
    List<Paper> searchByKeyword(String keyword) {
        try {
            List<Paper> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title", "essAbs", "keywords", "field").query(keyword)));
            SearchRequest searchRequest = new SearchRequest.Builder().index("paper").query(query).build();
            SearchResponse<Paper> searchResponse = client.search(searchRequest, Paper.class);
            for (Hit<Paper> hit : searchResponse.hits().hits()) {
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
     * @param papers 搜索的结果
     * @param pageNum 一页的条目数量
     * @param offset 第几页
     * @return 文献信息
     */
    List<Paper> searchByPage(List<Paper> papers, Integer pageNum, Integer offset) {
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
        List<Paper> sublist = papers.subList(startIndex, endIndex);
        if (sublist.isEmpty()) {
            return Collections.emptyList();
        }
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        for(Paper paper : papers) {
//            Map<String, Object> map = getPaperMap(paper);
//            mapList.add(map);
//        }
//        return mapList;
        return sublist;
    }

    /**
     * 排序
     * @param papers 搜索的结果
     * @param sortType 根据这个来进行排序 // 1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @return 文献信息
     */
    List<Paper> searchByOrder(List<Paper> papers, Integer sortType) {
        papers.sort(new Comparator<Paper>() {
            @Override
            public int compare(Paper p1, Paper p2) {
                if (sortType == 1) {
                    return p2.getPublishDate().compareTo(p1.getPublishDate());
                } else if (sortType == 2) {
                    return p2.getRef_times() - p1.getRef_times();
                } else if (sortType == 3) {
                    return p2.getFav_time() - p1.getFav_time();
                }
                return 0;
            }
        });
        return papers;
    }

    List<String> generateRef(Map<String, Integer> auths, String title, String derivation) {
        List<String> references = new ArrayList<>();
        String ref = "";
        for (String auth : auths.keySet()) {
            ref = ref + auth + ",";
        }
        ref += ". " + title + ". " + derivation;

        references.add(ref);
        return references;
    }
}

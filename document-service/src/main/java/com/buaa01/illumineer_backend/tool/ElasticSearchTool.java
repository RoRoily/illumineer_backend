package com.buaa01.illumineer_backend.tool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.buaa01.illumineer_backend.entity.ElasticSearchPaper;
import com.buaa01.illumineer_backend.entity.ElasticSearchWord;
import com.buaa01.illumineer_backend.entity.Paper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ElasticSearchTool {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * 将关键词word添加入es中
     * 每次调用将创建一个新的文档，这些文档的索引是"search_word"，但是id不同(默认处理)
     * @param word 关键词
     */
    public void addSearchWord(String word) {
        try{
            ElasticSearchWord elasticSearchWord = new ElasticSearchWord(word);
            elasticsearchClient.index(i->i.index("search_word").document(elasticSearchWord));
        }catch (IOException e){
            log.error("添加关键词至ElasticSearch出错：{}", e.getMessage());
        }
    }

    /**
     * 将paper添加入es中
     * @param paper 论文
     */
    public void addPaper(Paper paper) {
        try{
            ElasticSearchPaper esPaper = new ElasticSearchPaper(
                    paper.getPid(),
                    paper.getTitle(),
                    paper.getAuths(),
                    paper.getField(),
                    paper.getType(),
                    paper.getTheme(),
                    paper.getPublishDate(),
                    paper.getDerivation(),
                    paper.getRefTimes(),
                    paper.getFavTime(),
                    paper.getStats());
            elasticsearchClient.index(
                    i -> i.index("paper").id(esPaper.getPid().toString()).document(esPaper));
        }catch (IOException e){
            log.error("添加论文至ElasticSearch出错：{}", e.getMessage());
        }
    }

    /**
     * 删除论文
     * @param pid
     */
    public void deletePaper(Integer pid) {
        try{
            elasticsearchClient.delete(i->i.index("paper").id(pid.toString()));
        }catch (IOException e){
            log.error("删除论文至ElasticSearch出错：{}", e.getMessage());
        }
    }

    public void updatePaper(Paper paper) {
        try{
            ElasticSearchPaper esPaper = new ElasticSearchPaper(
                    paper.getPid(),
                    paper.getTitle(),
                    paper.getAuths(),
                    paper.getField(),
                    paper.getType(),
                    paper.getTheme(),
                    paper.getPublishDate(),
                    paper.getDerivation(),
                    paper.getRefTimes(),
                    paper.getFavTime(),
                    paper.getStats());
            elasticsearchClient.update(i->i.index("paper").id(paper.getPid().toString()).doc(esPaper),ElasticSearchPaper.class);
        }catch (IOException e){
            log.error("更新论文至ElasticSearch出错：{}", e.getMessage());
        }
    }

    /**
     * 模糊匹配，分页根据论文名查询
     * @param keyword   查询关键词
     * @param page  第几页 从1开始
     * @param size  每页查多少条数据 一般30条
     * @param onlyPass 是否只查询没有删除的论文
     * @return 包含查到的数据id列表，按匹配分数排序
     */
    public List<Long> searchPapersIdByTitle(String keyword, Integer page, Integer size, boolean onlyPass) {
        try {
            List<Long> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title").query(keyword).fuzziness("AUTO")));
            return getPaperIds(page, size, onlyPass, list, query);
        } catch (IOException e) {
            log.error("查询ES相关论文id时出错了：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 模糊匹配，分页根据作者名查询
     * @param keyword   查询关键词
     * @param page  第几页 从1开始
     * @param size  每页查多少条数据 一般30条
     * @param onlyPass 是否只查询没有删除的论文
     * @return 包含查到的数据id列表，按匹配分数排序
     */
    public List<Long> searchPapersIdByAuths(String keyword, Integer page, Integer size, boolean onlyPass) {
        try {
            List<Long> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("auths").query(keyword).fuzziness("AUTO")));
            return getPaperIds(page, size, onlyPass, list, query);
        } catch (IOException e) {
            log.error("查询ES相关论文id时出错了：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 将query与状态查询结合起来
    private List<Long> getPaperIds(Integer page, Integer size, boolean onlyPass, List<Long> list, Query query) throws IOException {
        Query query1 = Query.of(q -> q.constantScore(c -> c.filter(f -> f.term(t -> t.field("status").value(0)))));
        Query bool = Query.of(q -> q.bool(b -> b.must(query1).must(query)));
        SearchRequest searchRequest;
        if (onlyPass) {
            searchRequest = new SearchRequest.Builder().index("paper").query(bool).from((page - 1) * size).size(size).build();
        } else {
            searchRequest = new SearchRequest.Builder().index("paper").query(query).from((page - 1) * size).size(size).build();
        }
        SearchResponse<ElasticSearchPaper> searchResponse = elasticsearchClient.search(searchRequest, ElasticSearchPaper.class);
        for (Hit<ElasticSearchPaper> hit : searchResponse.hits().hits()) {
            if (hit.source() != null) {
                list.add(hit.source().getPid());
            }
        }
        return list;
    }

    /**
     * 模糊匹配，分页根据论文名查询
     * @param keyword   查询关键词
     * @param page  第几页 从1开始
     * @param size  每页查多少条数据 一般30条
     * @param onlyPass 是否只查询没有删除的论文
     * @return 包含查到的paper列表，按匹配分数排序
     */
    public List<ElasticSearchPaper> searchPapersByTitle(String keyword, Integer page, Integer size, boolean onlyPass) {
        try {
            List<ElasticSearchPaper> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title").query(keyword).fuzziness("AUTO")));
            return getPapers(page, size, onlyPass, list, query);
        } catch (IOException e) {
            log.error("查询ES相关论文时出错了：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 模糊匹配，分页根据作者名查询
     * @param keyword   查询关键词
     * @param page  第几页 从1开始
     * @param size  每页查多少条数据 一般30条
     * @param onlyPass 是否只查询没有删除的论文
     * @return 包含查到的paper列表，按匹配分数排序
     */
    public List<ElasticSearchPaper> searchPapersByAuths(String keyword, Integer page, Integer size, boolean onlyPass) {
        try {
            List<ElasticSearchPaper> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("auths").query(keyword).fuzziness("AUTO")));
            return getPapers(page, size, onlyPass, list, query);
        } catch (IOException e) {
            log.error("查询ES相关论文时出错了：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 将query与状态查询结合起来
    private List<ElasticSearchPaper> getPapers(Integer page, Integer size, boolean onlyPass, List<ElasticSearchPaper> list, Query query) throws IOException {
        Query query1 = Query.of(q -> q.constantScore(c -> c.filter(f -> f.term(t -> t.field("status").value(0)))));
        Query bool = Query.of(q -> q.bool(b -> b.must(query1).must(query)));
        SearchRequest searchRequest;
        if (onlyPass) {
            searchRequest = new SearchRequest.Builder().index("paper").query(bool).from((page - 1) * size).size(size).build();
        } else {
            searchRequest = new SearchRequest.Builder().index("paper").query(query).from((page - 1) * size).size(size).build();
        }
        SearchResponse<ElasticSearchPaper> searchResponse = elasticsearchClient.search(searchRequest, ElasticSearchPaper.class);
        for (Hit<ElasticSearchPaper> hit : searchResponse.hits().hits()) {
            if (hit.source() != null) {
                list.add(hit.source());
            }
        }
        return list;
    }

    /**
     * 获取推荐搜索词，包括全匹配，前向匹配和模糊匹配
     * @param word 关键词
     * @return 符合要求的list
     */
    public List<String> getMatchingWord(String word) {
        try {
            List<String> list = new ArrayList<>();
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("word").query(word).defaultOperator(Operator.And)));   // 关键词全匹配
            Query query1 = Query.of(q -> q.prefix(p -> p.field("word").value(word)));
            Query query2 = Query.of(q->q.fuzzy(f->f.field("word").value(word)));
            Query bool = Query.of(q -> q.bool(b -> b.should(query).should(query1).should(query2)));
            SearchRequest searchRequest = new SearchRequest.Builder().index("search_word").query(bool).from(0).size(10).build();
            SearchResponse<ElasticSearchWord> searchResponse = elasticsearchClient.search(searchRequest, ElasticSearchWord.class);
            for (Hit<ElasticSearchWord> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source().getWord());
                }
            }
            return list;
        } catch (IOException e) {
            log.error("获取ES搜索提示词时出错了：{}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

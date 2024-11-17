package com.buaa01.illumineer_backend.tool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.buaa01.illumineer_backend.entity.ES.ESUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.ES.ESPaper;
import com.buaa01.illumineer_backend.entity.ES.ESSearchWord;
import com.buaa01.illumineer_backend.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//modify by zmh 2024.11.13
@Component
@Slf4j
public class ESTool {
    @Autowired
    private ElasticsearchClient client;

    /**
     * 添加文章文档
     * @param paper
     */
    public void addPaper(Paper paper) throws IOException {
        try {
            ESPaper esPaper = new ESPaper(paper.getPid(), paper.getTitle(), paper.getKeywords(),
                    paper.getAuths(), paper.category2String(paper),paper.getType(),paper.getTheme(),paper.getPublishDate(),paper.getDerivation(), paper.getRef_times(), paper.getFav_time());
            client.index(i -> i.index("paper").id(esPaper.getPid().toString()).document(esPaper));
        } catch (IOException e) {
            log.error("添加视频文档到ElasticSearch时出错了：" + e);
            throw e;
        }
    }

    /**
     * 删除文章文档
     * @param pid
     */
    public void deletePaper(Integer pid) throws IOException {
        try {
            client.delete(d -> d.index("paper").id(pid.toString()));
        } catch (IOException e) {
            log.error("删除ElasticSearch视频文档时失败了：" + e);
            throw e;
        }
    }

    /**
     * 更新视频文档
     * @param paper
     */
    public void updatePaper(Paper paper) throws IOException {
        try {
            ESPaper esPaper = new ESPaper(paper.getPid(), paper.getTitle(), paper.getKeywords(),
                    paper.getAuths(), paper.category2String(paper),paper.getType(),paper.getTheme(),
                    paper.getPublishDate(),paper.getDerivation(), paper.getRef_times(), paper.getFav_time());
            client.update(u -> u.index("paper").id(paper.getPid().toString()).doc(esPaper), ESPaper.class);
        } catch (IOException e) {
            log.error("更新ElasticSearch视频文档时出错了：" + e);
            throw e;
        }
    }

    //-----------------更新到这里----------------//

    /**
     * 查询相关数据数量
     * @param keyword
     * @param onlyPass  是否只查询过审的
     * @return
     */
    public Long getPaperCount(String keyword, boolean onlyPass) {
        try {
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title", "tags").query(keyword)));
            Query query1 = Query.of(q -> q.constantScore(c -> c.filter(f -> f.term(t -> t.field("status").value(1)))));
            Query bool = Query.of(q -> q.bool(b -> b.must(query1).must(query)));
            CountRequest countRequest;
            if (onlyPass) {
                countRequest = new CountRequest.Builder().index("paper").query(bool).build();
            } else {
                countRequest = new CountRequest.Builder().index("paper").query(query).build();
            }
            CountResponse countResponse = client.count(countRequest);
            return countResponse.count();
        } catch (IOException e) {
            log.error("查询ES相关视频数量时出错了：" + e);
            return 0L;
        }
    }

    /**
     * 模糊匹配，分页查询
     * @param keyword   查询关键词
     * @param page  第几页 从1开始
     * @param size  每页查多少条数据 一般30条
     * @return 包含查到的数据id列表，按匹配分数排序
     */
    public List<Integer> searchPapersByKeyword(String keyword, Integer page, Integer size, boolean onlyPass) {
        try {
            List<Integer> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title", "tags").query(keyword)));
            Query query1 = Query.of(q -> q.constantScore(c -> c.filter(f -> f.term(t -> t.field("status").value(1)))));
            Query bool = Query.of(q -> q.bool(b -> b.must(query1).must(query)));
            SearchRequest searchRequest;
            if (onlyPass) {
                searchRequest = new SearchRequest.Builder().index("paper").query(bool).from((page - 1) * size).size(size).build();
            } else {
                searchRequest = new SearchRequest.Builder().index("paper").query(query).from((page - 1) * size).size(size).build();
            }
            SearchResponse<ESPaper> searchResponse = client.search(searchRequest, ESPaper.class);
            for (Hit<ESPaper> hit : searchResponse.hits().hits()) {
                list.add(hit.source().getPid());
            }
            return list;
        } catch (IOException e) {
            log.error("查询ES相关视频文档时出错了：" + e);
            return Collections.emptyList();
        }
    }


    /**
     * 添加用户文档
     * @param user
     */
    public void addUser(User user) throws IOException {
        try {
            ESUser esUser = new ESUser(user.getUid(), user.getNickName(), user.getInstitution(), user.getField());
            client.index(i -> i.index("user").id(esUser.getUid().toString()).document(esUser));
        } catch (IOException e) {
            log.error("添加用户文档到elasticsearch时出错了：" + e);
            throw e;
        }
    }

    /**
     * 删除视频文档
     * @param uid
     */
    public void deleteUser(Integer uid) throws IOException {
        try {
            client.delete(d -> d.index("user").id(uid.toString()));
        } catch (IOException e) {
            log.error("删除ElasticSearch用户文档时失败了：" + e);
            throw e;
        }
    }

    /**
     * 更新用户文档
     * @param user
     */
    public void updateUser(User user) throws IOException {
        try {
            ESUser esUser = new ESUser(user.getUid(), user.getNickName(), user.getInstitution(), user.getField());
            client.update(u -> u.index("user").id(user.getUid().toString()).doc(esUser), ESUser.class);
        } catch (IOException e) {
            log.error("更新ElasticSearch用户文档时出错了：" + e);
            throw e;
        }
    }

    /**
     * 查询相关用户数据数量
     * @param keyword
     * @return
     */
    public Long getUserCount(String keyword) {
        try {
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("nickname").query(keyword).defaultOperator(Operator.And)));
            CountRequest countRequest = new CountRequest.Builder().index("user").query(query).build();
            CountResponse countResponse = client.count(countRequest);
            return countResponse.count();
        } catch (IOException e) {
            log.error("查询ES相关用户数量时出错了：" + e);
            return 0L;
        }
    }

    /**
     * 模糊匹配，分页查询
     * @param keyword   查询关键词
     * @param page  第几页 从1开始
     * @param size  每页查多少条数据 一般30条
     * @return 包含查到的数据id列表，按匹配分数排序
     */
    public List<Integer> searchUsersByKeyword(String keyword, Integer page, Integer size) {
        try {
            List<Integer> list = new ArrayList<>();
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("nickname").query(keyword).defaultOperator(Operator.And)));
            SearchRequest searchRequest = new SearchRequest.Builder().index("user").query(query).from((page - 1) * size).size(size).build();
            SearchResponse<ESUser> searchResponse = client.search(searchRequest, ESUser.class);
            for (Hit<ESUser> hit : searchResponse.hits().hits()) {
                list.add(hit.source().getUid());
            }
            return list;
        } catch (IOException e) {
            log.error("查询ES相关用户文档时出错了：" + e);
            return Collections.emptyList();
        }
    }



    /**
     * 添加搜索词文档
     *
     * @param text
     */
    public void addSearchWord(String text) {
        try {
            ESSearchWord esSearchWord = new ESSearchWord(text);
            client.index(i -> i.index("search_word").document(esSearchWord));
        } catch (IOException e) {
            log.error("添加搜索词文档到elasticsearch时出错了：" + e);
        }
    }

    /**
     * 获取推荐搜索词
     * @param text
     * @return 推荐搜索词列表
     */
    public List<String> getMatchingWord(String text) {
        try {
            List<String> list = new ArrayList<>();
            //使用simpleQueryString
            //匹配content字段中完全包含text的文档，要求所有关键词匹配(Operator.And)
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("content").query(text).defaultOperator(Operator.And)));   // 关键词全匹配
            //使用prefix
            //以text为前缀来匹配content字段中的内容
            Query query1 = Query.of(q -> q.prefix(p -> p.field("content").value(text)));
            //两者之一查询成功
            Query bool = Query.of(q -> q.bool(b -> b.should(query).should(query1)));

            //创建新的Search Request
            //指定查询的索引为 search_word，从结果的第一个文档开始，返回最多10条数据
            SearchRequest searchRequest = new SearchRequest.Builder().index("search_word").query(bool).from(0).size(10).build();

            //转换成ESSearchWord类型的响应
            SearchResponse<ESSearchWord> searchResponse = client.search(searchRequest, ESSearchWord.class);
            //从每个 Hit 对象中获取搜索词的内容 hit.source().getContent()，并添加到 list 中。
            for (Hit<ESSearchWord> hit : searchResponse.hits().hits()) {
                list.add(hit.source().getContent());
            }
            return list;
        } catch (IOException e) {
            log.error("获取ES搜索提示词时出错了：" + e);
            return Collections.emptyList();
        }
    }
}

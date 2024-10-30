package com.buaa01.illumineer_backend.tool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.buaa01.illumineer_backend.entity.ElasticSearchScholar;
import com.buaa01.illumineer_backend.entity.ElasticSearchWord;
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
     * 将名字为name的学者添加入es中
     * @param name 名字
     */
    public void addScholar(String name) {
        try{
            ElasticSearchScholar scholar = new ElasticSearchScholar(name);
            elasticsearchClient.index(i->i.index("scholars").document(scholar));
        }catch (IOException e){
            log.error("添加学者名至ElasticSearch出错：{}", e.getMessage());
        }
    }

    /**
     * 获取名字匹配的学者list，模糊查询+前缀匹配
     * @param name 名字
     * @return 学者姓名的list
     */
    public List<String> getMatchedScholar(String name) {
        try{
            List<String> matchedScholar = new ArrayList<>();
            // 1. 创建关键词全匹配查询
            Query allQuery = Query.of(q -> q.simpleQueryString(s -> s
                    .fields("name")   // 查询的字段是 "content"
                    .query(name)         // 要查询的关键词是 word
                    .defaultOperator(Operator.And))); // 使用 AND 作为默认操作符，要求关键词全匹配
            // 2. 使用前缀查询，适合查找以 text 开头的名字
            Query prefixQuery = Query.of(q -> q
                    .prefix(p -> p
                            .field("name")     // 查询的字段是 "name" (假设学者名字字段为 "name")
                            .value(name)       // 关键词前缀是用户输入的 text
                    )
            );
            /*// 2. 使用通配符查询，适合查找部分匹配，'?' 匹配单个字符，'*' 匹配任意数量字符
            //TODO:过多使用通配符可能会产生性能问题
            Query wildcardQuery = Query.of(q -> q
                    .wildcard(w -> w
                            .field("name")     // 查询的字段是 "name"
                            .value("*" + name + "*") // 将输入作为通配符模式的一部分，允许以 text 开头的任意字符匹配
                    )
            );*/

            // 3. 使用模糊查询，允许轻微的拼写错误
            Query fuzzyQuery = Query.of(q -> q
                    .fuzzy(f -> f
                            .field("name")     // 查询字段 "name"
                            .value(name)       // 输入的文本
                            .fuzziness("AUTO") // 自动调整模糊程度
                    )
            );

            // 4. 构建布尔查询，组合以上四种查询
            Query boolQuery = Query.of(q -> q
                    .bool(b -> b
                            .should(prefixQuery)     // 前缀匹配
                            //.should(wildcardQuery)   // 通配符匹配
                            .should(fuzzyQuery)      // 模糊匹配
                            .should(allQuery)        // 全匹配
                    )
            );
            // 5. 构建搜索请求，查询索引 "scholars"
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("scholars")          // 索引为 "scholars"
                    .query(boolQuery)           // 使用组合查询
                    .from(0)                    // 从第 0 条记录开始
                    .size(10)                   // 返回最多 10 条结果
                    .build();
            SearchResponse<ElasticSearchScholar> searchResponse = elasticsearchClient.search(searchRequest, ElasticSearchScholar.class);
            for (Hit<ElasticSearchScholar> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    matchedScholar.add(hit.source().getName());
                }
            }
            return matchedScholar;
        }catch (IOException e){
            log.error("从ElasticSearch获取学者词出错：{}", e.getMessage());
            return Collections.emptyList();
        }
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

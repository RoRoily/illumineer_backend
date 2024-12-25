package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.AIAssistantService;
import com.buaa01.illumineer_backend.service.impl.paper.PaperSearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

    @Autowired
    PaperSearchServiceImpl paperSearchService;

    @Autowired
    public AIAssistantController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * AI推荐领域内关键词推荐
     * @param query 查询的领域
     * @return 返回关键词（用空格隔开）
     * @throws Exception 异常情况（AI接口调用异常）
     */
    @PostMapping("/AI/generateKeywords")
    public ResponseEntity<String> generateKeywords(@RequestParam String query) throws Exception{
        CompletableFuture<String> future = aiAssistantService.StartChat(
                "推荐“" + query + "”" +
                        "领域的2个英文关键词，尽量简短，每个占一行" +
                        "（只输出关键词，不要附加其他内容）");
        String keywords = future.get();
        keywords = keywords.replaceAll("[^a-zA-Z\\s\\n]", "");
        System.out.println("keywords: " + keywords);
        return ResponseEntity.ok(future.get());
    }

    /**
     * AI领域关键字直接搜索（高级搜索）
     * @param query 用户生成的描述性内容，传给AI总结关键词
     * @param size 一页多少条内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
     * @return 文献信息
     * @throws Exception 异常情况（AI接口调用异常）
     */
    @PostMapping("/AI/searchPaper")
    public CustomResponse searchPaper(@RequestParam("query") String query,
                                      @RequestParam("size") Integer size,
                                      @RequestParam("offset") Integer offset,
                                      @RequestParam("type") Integer sortType,
                                      @RequestParam("order") Integer order) throws Exception{
        CompletableFuture<String> future = aiAssistantService.StartChat(
                "推荐“" + query + "”" +
                        "领域的2个英文关键词，尽量简短，每个占一行" +
                        "（只输出关键词，不要附加其他内容）");
        String keywords = future.get();
        keywords = keywords.replaceAll("[^a-zA-Z\\s\\n]", "");
        System.out.println("keywords: " + keywords);
        String[] keywordSplit = keywords.split("\\n+");
        // 使用正则表达式来识别分割
        List<String> keywordList = Arrays.stream(keywordSplit).collect(Collectors.toList());
        List<String> logicList = new ArrayList<>();
        List<String> conditionList = new ArrayList<>();
        for (int i = 0; i < keywordList.size(); i++) {
            if (i == 0) {
                logicList.add("0");
            }
            else {
                logicList.add("2"); // or instead of and
            }
            conditionList.add("title");
        }
        for (String condition: conditionList) {
            System.out.print(condition + ",");
        }
        System.out.println();
        for (String logic: logicList) {
            System.out.print(logic + ",");
        }
        System.out.println();
        for (String keyword: keywordList) {
            System.out.print(keyword + ",");
        }
        System.out.println();

        String logic = String.join(",", logicList);
        String condition = String.join(",", conditionList);
        String keyword = String.join(",", keywordList);
        Object data = paperSearchService.advancedSearchPapers(logic, condition, keyword, size, offset, sortType, order).getData();
        Map<String, Object> result = (Map<String, Object>) data;
        result.put("generatedKeywords", keywordList);
        return new CustomResponse(200, "OK", result);
    }
}
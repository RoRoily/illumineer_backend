package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.AIAssistantService;
import com.buaa01.illumineer_backend.service.impl.paper.PaperSearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/document")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

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
        String keywords = aiAssistantService.StartChat("我想调查“" + query + "”领域的论文资料，请帮我推荐相关的关键词，尽量简短，只用空格隔开。");
        return ResponseEntity.ok(keywords);
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
        String keywords = aiAssistantService.StartChat("我想调查“" + query + "”领域的论文资料，请帮我推荐相关的关键词，尽量简短，只用空格隔开。");
        String[] keywordSplit = keywords.split("[ 、。，,.]+");// 我不清楚AI会写出什么分割符（一般是、），所以使用正则表达式来识别
        List<String> keywordList = Arrays.stream(keywordSplit).toList();
        List<Integer> logicList = new ArrayList<>();
        List<String> conditionList = new ArrayList<>();
        for (int i = 0; i < keywordList.size(); i++) {
            if (i == 0) logicList.add(0);
            else logicList.add(1);
            conditionList.add("keywords");
        }
        Object data = new PaperSearchServiceImpl().advancedSearchPapers(logicList, conditionList, keywordList, size, offset, sortType, order).getData();
        Map<String, Object> result;
        if (isMapOfStringToObject(data)) {
            result = (Map<String, Object>) data;
            result.put("generatedKeywords", keywordList);
            return new CustomResponse(200, "OK", result);
        }
        else {
            return new CustomResponse(500, "Internal Error", null);
        }
    }

    public static boolean isMapOfStringToObject(Object obj) {
        // 1. 检查 obj 是否是一个 Map 实例
        if (obj instanceof Map<?, ?> map) {

            // 2. 检查键是否为 String 类型，值是否为 Object 类型
            Set<?> keys = map.keySet();
            for (Object key : keys) {
                if (!(key instanceof String)) {
                    return false;  // 键不是 String 类型
                }
                // 值的类型通常是 Object，所以不需要显式检查
            }
            return true; // 所有键都是 String 类型
        }
        return false;  // obj 不是 Map 类型
    }

}
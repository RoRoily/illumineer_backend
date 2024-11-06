package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/document")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

    @Autowired
    public AIAssistantController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * AI推荐领域内关键词
     * @param query 查询的领域
     * @return 返回关键词（用空格隔开）
     * @throws Exception 异常情况
     */
    @PostMapping("/AI/generateKeywords")
    public ResponseEntity<String> generateKeywords(@RequestParam String query) throws Exception{
        String keywords = aiAssistantService.StartChat("我想调查“" + query + "”领域的论文资料，请帮我推荐相关的关键词，尽量简短，只用空格隔开。");
        return ResponseEntity.ok(keywords);
    }
}
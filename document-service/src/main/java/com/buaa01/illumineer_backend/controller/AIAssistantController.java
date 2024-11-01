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

    @PostMapping("/AI/generateKeywords")
    public ResponseEntity<String> generateKeywords(@RequestParam String query) throws Exception{
        String keywords = aiAssistantService.StartChat(query);
        return ResponseEntity.ok(keywords);
    }
}
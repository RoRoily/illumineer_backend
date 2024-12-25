package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;


@RestController
public class SearchRecommendController {
    @Autowired
    private SearchService searchService;


    /**
     * 添加搜索词或者给该搜索词热度加一
     * @param keyword   搜索词
     * @return  返回格式化后的搜索词，有可能为null
     */
    @PostMapping("/search/word/add")
    public CustomResponse addSearchWord(@RequestParam("keyword") String keyword) {
        System.out.println("keyword:" + keyword);
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(searchService.addSearchWord(keyword));
        return customResponse;
    }
    /**
     * 根据输入内容获取相关搜索推荐词
     * @param keyword   关键词
     * @return  包含推荐搜索词的列表
     */
    @GetMapping("/search/word/get")
    public CustomResponse getSearchWord(@RequestParam("keyword") String keyword) throws UnsupportedEncodingException {
        keyword = URLDecoder.decode(keyword, "UTF-8");  // 解码经过url传输的字符串
        System.out.println("keyword used to get Recommend:" + keyword);
        CustomResponse customResponse = new CustomResponse();
        if (keyword.trim().length() == 0) {
            customResponse.setData(Collections.emptyList());
        } else {
            customResponse.setData(searchService.getMatchingWord(keyword));
            System.out.println("Recommend" + searchService.getMatchingWord(keyword));
        }
        return customResponse;
    }
}

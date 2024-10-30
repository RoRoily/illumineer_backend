package com.buaa01.illumineer_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Map;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Papers;
import com.buaa01.illumineer_backend.entity.ScreenCondition;
import com.buaa01.illumineer_backend.service.filter.FilterService;

@RestController
public class FilterController {

    @Autowired
    private FilterService filterService;

    /**
     * ES模糊搜索结果的进一步筛选
     * 模糊->限定
     * 
     * @param ArrayList<Paper>   ES模糊搜索结果List
     * @param Map<String,ArrayList<String>> 筛选条件
     * 
     * @return CustomResponse对象
     */

    @PostMapping("paper/search/filter")
    public CustomResponse ResultFilter(@RequestParam ArrayList<Papers> papers,
            @RequestBody Map<String, ArrayList<String>> screencondition) {

        ScreenCondition sc = new ScreenCondition(screencondition);
        try {
            return filterService.ResultFilter(papers, sc);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("筛选过程出现错误！");
            return customResponse;
        }
    }
}
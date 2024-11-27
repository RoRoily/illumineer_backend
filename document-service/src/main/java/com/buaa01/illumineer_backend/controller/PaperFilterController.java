package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.paper.PaperFilterService;
import com.buaa01.illumineer_backend.utils.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class PaperFilterController {

    @Autowired
    private PaperFilterService filterService;

    /**
     * ES模糊搜索结果的进一步筛选
     * 模糊->限定
     * 
     * @param Map<String,ArrayList<String>> 筛选条件
     * 
     * @return CustomResponse对象
     */

    @PostMapping("get/filter")
    public CustomResponse ResultFilter(@RequestBody Map<String, ArrayList<String>> filtercondition) {

        FilterCondition sc = new FilterCondition(filtercondition);
        try {
            return filterService.filterSearchResult(sc);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("筛选过程出现错误！");
            return customResponse;
        }
    }
}
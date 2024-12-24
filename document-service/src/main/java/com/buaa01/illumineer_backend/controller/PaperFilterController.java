package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.service.paper.PaperFilterService;
import com.buaa01.illumineer_backend.utils.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PaperFilterController {

    @Autowired
    private PaperFilterService filterService;

    /**
     * ES模糊搜索结果的进一步筛选
     * 模糊->限定
     *
     * @param Map<String, ArrayList<String>> 筛选条件
     * @param size        一页的条数
     * @param offset      第几页
     * @param sortType    根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order       0=降序，1=升序
     * 
     * @return Map<String, Object> 筛选结果,其中resultPapers为文章，total为筛选结果的总数
     */

    @PostMapping("/get/filter")
    public CustomResponse ResultFilter(@RequestParam("size") Integer size,
            @RequestParam("offset") Integer offset,
            @RequestParam("sortType") Integer sortType,
            @RequestParam("order") Integer order,
            @RequestBody Map<String, Object> filtercondition) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> resultPapers = filterService.filterSearchResult(filtercondition, size, offset, sortType, order);
            customResponse.setData(resultPapers);
            return customResponse;
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("筛选过程出现错误！");
            return customResponse;
        }
    }
}
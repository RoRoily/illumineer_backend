package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.service.paper.PaperSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PaperSearchController {

    @Autowired
    private PaperSearchService paperSearchService;

    /**
     * 根据pid获取文献信息
     * @param pid 文献ID
     * @return Paper
     */
    @GetMapping("/get/id")
    public CustomResponse getPaperByPid(@RequestParam("pid") Long pid) {
        try {
            return paperSearchService.getPaperByPid(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法根据pid获取文献信息！");
            return customResponse;
        }
    }

    /**
    * @description: 根据stats返回相应的Paper
    * @param: [stats 状态, size 一页的条数, offset 第几页, sortType 排序依据, order 升序/降序]
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
    * @return: Paper
    **/
    @GetMapping("/get/stats")
    public CustomResponse getPaperByStats(@RequestParam("stats") Integer stats,
                                          @RequestParam("size") Integer size,
                                          @RequestParam("offset") Integer offset,
                                          @RequestParam("sortType") Integer sortType,
                                          @RequestParam("order") Integer order) {
        try {
            return paperSearchService.getPaperByStats(stats, size, offset, sortType, order);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法根据文章状态获取paper");
            return customResponse;
        }
    }

    /**
     * 一框式检索接口：搜索文献（分页、排序）
     * @param condition 筛选条件（选择查找的字段）
     * @param keyword 搜索内容
     * @param size 一页多少条内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
     * @return SearchResultPaper
     */
    @GetMapping("/get/keyword")
    public CustomResponse searchPapers(@RequestParam("condition") String condition,
                                       @RequestParam("keyword") String keyword,
                                       @RequestParam("size") Integer size,
                                       @RequestParam("offset") Integer offset,
                                       @RequestParam("sortType") Integer sortType,
                                       @RequestParam("order") Integer order) {
        try {
            return paperSearchService.searchPapers(condition, keyword, size, offset, sortType, order);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取文献信息！");
            return customResponse;
        }
    }

    /**
     * 高级检索
     * @param logic none=0/and=1/or=2/not=3
     * @param condition
     * @param keyword（传 name 或者 %name%）
     * @param size 一页多少条内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order 0=降序，1=升序
     * @return SearchResultPaper
     */
    @GetMapping("get/advanced")
    public CustomResponse advancedSearchPapers(@RequestParam("logic") String logic,
                                               @RequestParam("condition") String condition,
                                               @RequestParam("keyword") String keyword,
                                               @RequestParam("size") Integer size,
                                               @RequestParam("offset") Integer offset,
                                               @RequestParam("sortType") Integer sortType,
                                               @RequestParam("order") Integer order) {
        try {
            return paperSearchService.advancedSearchPapers(logic, condition, keyword, size, offset, sortType, order);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取文献信息！");
            return customResponse;
        }
    }
}

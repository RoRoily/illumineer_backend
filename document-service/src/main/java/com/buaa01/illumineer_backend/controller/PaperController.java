package com.buaa01.illumineer_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PaperController {

    @Autowired
    private PaperService paperService;

    /**
     * 根据pid获取文献信息
     * @param pid 文献ID
     * @return 文献信息
     */
    @GetMapping("/get")
    public CustomResponse getPaperByPid(@RequestParam("pid") Integer pid) {
        try {
            return paperService.getPaperByPid(pid);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法根据pid获取文献信息！");
            return customResponse;
        }
    }

    /**
     * 一框式检索接口：搜索文献（分页、排序）
     * @param keyword 搜索内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @return 文献信息
     */
    @GetMapping("/search")
    public CustomResponse searchPapers(@RequestParam("keyword") String keyword,
                                          @RequestParam("offset") Integer offset,
                                          @RequestParam("type") Integer sortType) {
        try {
            return paperService.searchPapers(keyword, offset, sortType);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("无法获取文献信息！");
            return customResponse;
        }
    }
}

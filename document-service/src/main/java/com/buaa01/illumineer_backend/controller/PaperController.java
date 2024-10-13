package com.buaa01.illumineer_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PaperController {

    @Autowired
    private PaperMapper paperMapper;

    /**
     * 根据pid获取文献信息
     * @param pid 文献ID
     * @return 文献信息
     */
    @GetMapping("/paper/get")

    public CustomResponse getPaperByPid(@RequestParam("pid") Integer pid) {
        CustomResponse responseResult = new CustomResponse();
        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        paper = paperMapper.selectOne(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("essAbs", paper.getEssAbs());
        map.put("contentUrl",paper.getContentUrl());
        map.put("title",paper.getTitle());
        responseResult.setData(map);
        return responseResult;
    }
}

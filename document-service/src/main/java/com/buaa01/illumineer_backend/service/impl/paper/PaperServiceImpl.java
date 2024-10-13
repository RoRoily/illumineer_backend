package com.buaa01.illumineer_backend.service.impl.paper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.mapper.PaperMapper;
import com.buaa01.illumineer_backend.service.paper.PaperService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaperServiceImpl implements PaperService {

    private PaperMapper paperMapper;

    @Override
    public CustomResponse getPaperByPid(Integer pid) {
        CustomResponse customResponse = new CustomResponse();
        Paper paper = null;
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
//        paper = paperMapper.selectOne(queryWrapper);
        paper = paperMapper.getPaperByPid(pid);
        Map<String, Object> map = new HashMap<>();
        map.put("title",paper.getTitle());
        map.put("essAbs", paper.getEssAbs());
        map.put("keywords",paper.getKeywords());
        map.put("contentUrl",paper.getContentUrl());
        map.put("auths",paper.getAuths());
        map.put("field",paper.getField());
        map.put("publishDate",paper.getPublishDate());
        map.put("derivation",paper.getDerivation());
        map.put("ref_times", paper.getRef_times());
        map.put("fav_times",paper.getFav_time());
        map.put("refs",paper.getRefs());
        customResponse.setData(map);
        return customResponse;
    }

    @Override
    public CustomResponse getPapersByAttr(String attr, String value) {
        CustomResponse customResponse = new CustomResponse();
        List<Paper> papers = null;
        List<Map<String, Object>> paperMap = new ArrayList<>();
        QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(attr, value);
        papers = paperMapper.selectList(queryWrapper);
        for (Paper paper : papers) {
            Map<String, Object> map = new HashMap<>();
            map.put("title",paper.getTitle());
            map.put("essAbs", paper.getEssAbs());
            map.put("keywords",paper.getKeywords());
            map.put("contentUrl",paper.getContentUrl());
            map.put("auths",paper.getAuths());
            map.put("field",paper.getField());
            map.put("publishDate",paper.getPublishDate());
            map.put("derivation",paper.getDerivation());
            map.put("ref_times", paper.getRef_times());
            map.put("fav_times",paper.getFav_time());
            map.put("refs",paper.getRefs());
            paperMap.add(map);
        }

        customResponse.setData(paperMap);
        return customResponse;
    }

    @Override
    public CustomResponse getPapersByAttrs(List<String> attrs, List<String> values) {
        CustomResponse customResponse = new CustomResponse();
        List<Map<String, Object>> paperMap = new ArrayList<>();

        int size = attrs.size();
        for (int i=0; i<size; i++) {
            List<Paper> papers = null;
            QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(attrs.get(i), values.get(i));
            papers = paperMapper.selectList(queryWrapper);
            for (Paper paper : papers) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", paper.getTitle());
                map.put("essAbs", paper.getEssAbs());
                map.put("keywords", paper.getKeywords());
                map.put("contentUrl", paper.getContentUrl());
                map.put("auths", paper.getAuths());
                map.put("field", paper.getField());
                map.put("publishDate", paper.getPublishDate());
                map.put("derivation", paper.getDerivation());
                map.put("ref_times", paper.getRef_times());
                map.put("fav_times", paper.getFav_time());
                map.put("refs", paper.getRefs());
                paperMap.add(map);
            }
        }

        customResponse.setData(paperMap);
        return customResponse;
    }
}

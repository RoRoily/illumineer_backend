package com.buaa01.illumineer_backend.service.impl.filter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.ScreenCondition;
import com.buaa01.illumineer_backend.service.filter.FilterService;

import java.util.*;
import java.util.stream.*;

public class FilterServiceImpl implements FilterService {

    @Override
    public CustomResponse ResultFilter(ArrayList<Paper> papers, ScreenCondition sc) {

        List<Paper> filteredPapers = papers.stream()
                .filter(paper -> (sc.getYear().isEmpty()
                        || sc.getYear().contains(transDatetoYear(paper.getPublishDate()))) &&
                        (sc.getType().isEmpty() || sc.getType().contains(paper.getType())) &&
                        (sc.getDerivation().isEmpty() || sc.getDerivation().contains(paper.getDerivation())) &&
                        (sc.getTheme().isEmpty() || sc.getTheme().contains(paper.getTheme())))
                .collect(Collectors.toList());

        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(filteredPapers);
        return customResponse;
    }

    public String transDatetoYear(Date date) { // 将发布日期转化为年
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        return String.valueOf(year);
    }

}

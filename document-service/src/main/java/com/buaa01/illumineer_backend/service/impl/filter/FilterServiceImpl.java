package com.buaa01.illumineer_backend.service.impl.filter;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Papers;
import com.buaa01.illumineer_backend.entity.ScreenCondition;
import com.buaa01.illumineer_backend.service.filter.FilterService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class FilterServiceImpl implements FilterService {

    @Override
    public CustomResponse ResultFilter(ArrayList<Papers> papers, ScreenCondition sc) {

        List<Papers> filteredPapers = papers.stream()
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

    public String transDatetoYear(LocalDate date) { // 将发布日期转化为年
        return String.valueOf(date.getYear());
    }
}

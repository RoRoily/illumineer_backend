package com.buaa01.illumineer_backend.service.impl.filter;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Papers;
import com.buaa01.illumineer_backend.entity.ScreenCondition;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.service.filter.FilterService;
import com.buaa01.illumineer_backend.service.impl.paper.PaperServiceImpl;
import com.buaa01.illumineer_backend.service.paper.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

@Service
public class FilterServiceImpl implements FilterService {

    @Autowired
    private PaperServiceImpl paperServiceImpl;

    @Override
    public CustomResponse ResultFilter(ScreenCondition sc) {
        List<SearchResultPaper> papers = paperServiceImpl.getFromRedis();

        List<SearchResultPaper> filteredPapers = papers.stream()
                .filter(paper -> (sc.getYear().isEmpty()
                        || sc.getYear().contains(transDatetoYear(paper.getPublishDate()))) &&
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

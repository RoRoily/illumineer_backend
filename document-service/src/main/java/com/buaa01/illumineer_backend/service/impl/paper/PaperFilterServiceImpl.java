package com.buaa01.illumineer_backend.service.impl.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.service.paper.PaperFilterService;
import com.buaa01.illumineer_backend.utils.FilterCondition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaperFilterServiceImpl implements PaperFilterService {

    @Autowired
    private PaperSearchServiceImpl paperSearchServiceImpl;

    @Override
    public CustomResponse filterSearchResult(FilterCondition sc) {
        List<SearchResultPaper> papers = paperSearchServiceImpl.getFromRedis();

        List<SearchResultPaper> filteredPapers = papers.stream()
                .filter(paper -> (sc.getYear().isEmpty()
                        || sc.getYear().contains(String.valueOf(paper.getPublishDate().getYear()))) &&
                        (sc.getDerivation().isEmpty() || sc.getDerivation().contains(paper.getDerivation())) &&
                        (sc.getTheme().isEmpty() || sc.getTheme().contains(paper.getTheme())))
                .collect(Collectors.toList());

        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(filteredPapers);
        return customResponse;
    }
}

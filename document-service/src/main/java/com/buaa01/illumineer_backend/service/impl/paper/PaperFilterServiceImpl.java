package com.buaa01.illumineer_backend.service.impl.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.service.paper.PaperFilterService;
import com.buaa01.illumineer_backend.utils.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaperFilterServiceImpl implements PaperFilterService {

    @Autowired
    private PaperSearchServiceImpl paperSearchServiceImpl;

    @Override
    public List<SearchResultPaper> filterSearchResult(FilterCondition sc, Integer size, Integer offset,
            Integer sortType,
            Integer order) {

        boolean isYearEmpty = sc.getYear().isEmpty();
        boolean isDerivationEmpty = sc.getDerivation().isEmpty();
        boolean isThemeEmpty = sc.getTheme().isEmpty();

        List<SearchResultPaper> filteredPapers = paperSearchServiceImpl.getFromRedis().parallelStream()
                .filter(paper -> (isYearEmpty
                        || sc.getYear().contains(String.valueOf(paper.getPublishDate().getYear()))) &&
                        (isDerivationEmpty || sc.getDerivation().contains(paper.getDerivation())) &&
                        (isThemeEmpty || sc.getTheme().contains(paper.getTheme())))
                .collect(Collectors.toList());

        List<SearchResultPaper> papers = sortPapers(filteredPapers, sortType, order);
        List<SearchResultPaper> sortedPapers = paperSearchServiceImpl.searchByPage(papers, size, offset);

        return sortedPapers;
    }

    List<SearchResultPaper> sortPapers(List<SearchResultPaper> papers, Integer sortType, Integer order) {
        Comparator<SearchResultPaper> comparator;
        if (sortType == 1) {
            comparator = Comparator.comparing(SearchResultPaper::getPublishDate);
        } else if (sortType == 2) {
            comparator = Comparator.comparingInt(SearchResultPaper::getRefTimes);
        } else if (sortType == 3) {
            comparator = Comparator.comparingInt(SearchResultPaper::getFavTime);
        } else {
            return papers;
        }

        if (order == 0) { // 降序
            comparator = comparator.reversed();
        }

        papers.sort(comparator);
        return papers;
    }

}

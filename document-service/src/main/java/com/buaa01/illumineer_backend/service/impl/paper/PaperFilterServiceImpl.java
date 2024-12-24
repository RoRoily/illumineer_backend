package com.buaa01.illumineer_backend.service.impl.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.service.paper.PaperFilterService;
import com.buaa01.illumineer_backend.utils.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaperFilterServiceImpl implements PaperFilterService {

    @Autowired
    private PaperSearchServiceImpl paperSearchServiceImpl;

    @Override
    public Map<String, Object> filterSearchResult(FilterCondition sc, Integer size, Integer offset,
            Integer sortType, Integer order) {

        boolean isYearEmpty = sc.getYear().isEmpty();
        boolean isDerivationEmpty = sc.getDerivation().isEmpty();
        boolean isTypeEmpty = sc.getType().isEmpty();
        boolean isThemeEmpty = sc.getTheme().isEmpty();

        Set<String> filterYears = isYearEmpty ? Collections.emptySet() : new HashSet<>(sc.getYear());
        Set<String> filterDerivations = isDerivationEmpty ? Collections.emptySet() : new HashSet<>(sc.getDerivation());
        Set<String> filterTypes = isTypeEmpty ? Collections.emptySet() : new HashSet<>(sc.getType());
        Set<String> filterThemes = isThemeEmpty ? Collections.emptySet() : new HashSet<>(sc.getTheme());

        List<SearchResultPaper> filteredPapers = paperSearchServiceImpl.getFromRedis().parallelStream()
                .filter(paper -> {
                    Integer PublishYear = paper.getPublishDate().getYear() + 1900;
                    boolean matchesYear = isYearEmpty || filterYears.contains(PublishYear.toString());
                    boolean matchesDerivation = isDerivationEmpty || filterDerivations.contains(paper.getDerivation());
                    boolean matchesType = isTypeEmpty || filterTypes.contains(paper.getType());
                    boolean matchesTheme = isThemeEmpty || filterThemes.contains(paper.getTheme());
                    return matchesYear && matchesDerivation && matchesType && matchesTheme;
                })
                .collect(Collectors.toList());

        List<SearchResultPaper> sortedPapers = sortPapers(filteredPapers, sortType, order);
        List<SearchResultPaper> resultPapers = paperSearchServiceImpl.searchByPage(sortedPapers, size, offset);

        HashMap<String, Object> returnValues = new HashMap<>();
        returnValues.put("resultPapers", resultPapers);
        returnValues.put("total", sortedPapers.size());
        return returnValues;
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
            return papers; // 如果没有匹配的排序类型，直接返回原始列表
        }

        if (order == 0) { // 降序
            comparator = comparator.reversed();
        }

        SearchResultPaper[] paperArray = papers.toArray(new SearchResultPaper[0]);
        Arrays.parallelSort(paperArray, comparator);

        return Arrays.asList(paperArray);

        // return papers.parallelStream()
        // .sorted(comparator)
        // .collect(Collectors.toList());
    }

}

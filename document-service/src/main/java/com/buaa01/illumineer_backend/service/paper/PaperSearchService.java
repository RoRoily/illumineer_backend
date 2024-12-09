package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;

import java.util.List;
import java.util.Map;

public interface PaperSearchService {

    CustomResponse getPaperByPid(Long pid);

    CustomResponse getPaperByStats(Integer stats, Integer size, Integer offset, Integer sortType, Integer order);

    CustomResponse searchPapers(String condition, String keyword, Integer size, Integer offset, Integer sortType, Integer order);

    CustomResponse advancedSearchPapers(List<Integer> logic, List<String> condition, List<String> keyword, Integer size, Integer offset, Integer sortType, Integer order);
}

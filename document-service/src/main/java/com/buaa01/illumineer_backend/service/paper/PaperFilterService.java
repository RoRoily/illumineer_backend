package com.buaa01.illumineer_backend.service.paper;

import java.util.List;
import java.util.Map;

import com.buaa01.illumineer_backend.entity.SearchResultPaper;
import com.buaa01.illumineer_backend.utils.FilterCondition;

public interface PaperFilterService {
    /**
     * 根据筛选条件对模糊搜索到的List进行筛选
     * 
     * @param ArrayList<FilterCondition> 筛选条件列表
     * @param sortType                   排序依据
     *                                   1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @param order                      0=降序，1=升序
     * 
     * @return List<SearchResultPaper>
     */
    Map<String, Object> filterSearchResult(Map<String, Object> sc, Integer size, Integer offset, Integer sortType,
            Integer order);
}

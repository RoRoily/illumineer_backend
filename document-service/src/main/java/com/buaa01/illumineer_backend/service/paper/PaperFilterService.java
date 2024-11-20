package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.utils.FilterCondition;

public interface PaperFilterService {
    /**
     * 根据筛选条件对模糊搜索到的List进行筛选
     * @param ArrayList<FilterCondition> 筛选条件列表
     * 
     * @return CustomResponse
     */
    CustomResponse filterSearchResult(FilterCondition sc);
}

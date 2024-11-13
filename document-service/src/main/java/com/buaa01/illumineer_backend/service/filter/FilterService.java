package com.buaa01.illumineer_backend.service.filter;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.ScreenCondition;

import java.util.ArrayList;

public interface FilterService {
    /**
     * 根据筛选条件对模糊搜索到的List进行筛选
     * @param ArrayList<ScreenCondition> 筛选条件列表
     * 
     * @return CustomResponse
     */
    CustomResponse ResultFilter(ScreenCondition sc);
}

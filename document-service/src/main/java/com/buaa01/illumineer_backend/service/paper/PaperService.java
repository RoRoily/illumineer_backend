package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Paper;

import java.util.List;

public interface PaperService {
    /**
     * 根据pid查询文献信息
     * @param pid 文献ID
     * @return Paper 文献实体类
     */
    CustomResponse getPaperByPid(Integer pid);

    /**
     * 一框式检索接口：搜索文献（分页、排序）
     * @param keyword 搜索内容
     * @param offset 第几页
     * @param sortType 根据什么进行排序：1=publishDate出版时间，2=ref_times引用次数，3=fav_time收藏次数
     * @return 文献信息
     */
    CustomResponse searchPapers(String keyword, Integer offset, Integer sortType);

}

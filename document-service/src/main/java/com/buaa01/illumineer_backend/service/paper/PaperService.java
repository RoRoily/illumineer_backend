package com.buaa01.illumineer_backend.service.paper;

import com.buaa01.illumineer_backend.entity.Paper;

public interface PaperService {
    /**
     * 根据pid查询文献信息
     * @param pid 文献ID
     * @return Paper 文献实体类
     */
    Paper getPaperByPid(Integer pid);
}

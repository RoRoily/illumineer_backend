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
     * 根据（属性是否等于某个值）获取文献信息
     * @param map 包含attrs和values：String
     * @return 文献信息
     */
    CustomResponse getPapersByAttr(String attr, String value);

    /**
     * 根据（属性是否等于某个值）获取文献信息
     * @param map 包含attrs和values：List
     * @return 文献信息
     */
    CustomResponse getPapersByAttrs(List<String> attrs, List<String> values);
}

package com.buaa01.illumineer_backend.service.history;

import com.buaa01.illumineer_backend.entity.CustomResponse;

import java.util.List;
import java.util.Map;

public interface HistoryService {
    /**
     * 分页返回历史记录中的条目
     */
    public List<Map<String, Object>> getHistoryByPage(Integer uid, Integer quantity, Integer offset);

    /**
     *  在历史记录中新增条目
     */
    public CustomResponse insertInHistory(Integer pid);

    /**
     *  在历史记录中删除条目
     * */
    public CustomResponse deleteInHistory();
}

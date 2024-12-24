package com.buaa01.illumineer_backend.service.history;

import com.buaa01.illumineer_backend.entity.CustomResponse;

import java.util.List;
import java.util.Map;

public interface HistoryService {
    /**
     * 分页返回历史记录中的条目
     */
    public CustomResponse getHistoryByPage(Integer uid);

    /**
     *  在历史记录中新增条目
     */
    public CustomResponse insertInHistory(Integer userID, Long pid);

    /**
     *  在历史记录中删除条目
     * */
    public CustomResponse deleteInHistory(Integer userID, List<Long> pids);
}

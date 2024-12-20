package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;

public interface AppealService {

    /**
     * 创建新的待处理冲突条目
     * @param appellantUid
     * @param sameName
     * @param conflictPaperPid
     * @return
     */
    public CustomResponse createAppealEntry(Integer appellantUid,String sameName,Integer conflictPaperPid);

    /**
     * 处理冲突条目
     * @param appealEntryId
     * @param acceptAppeal
     * @return
     */
    public CustomResponse judgeAppeal(Integer appealEntryId,boolean acceptAppeal);

    /**
     * 分页查询冲突条目
     * @param quantity
     * @param index
     * @param handled
     * @return
     */
    public CustomResponse displayAppealEntry(Integer quantity,Integer index,boolean handled);

}

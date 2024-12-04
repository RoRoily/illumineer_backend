package com.buaa01.illumineer_backend.service.user;

import com.buaa01.illumineer_backend.entity.User2Paper;

public interface UserPaperService {
    /**
     * 更新访问次数以及最近访问时间，顺便返回记录信息，没有记录则创建新记录
     * @param uid   用户ID
     * @param vid   视频ID
     * @return 更新后的数据信息
     */
    User2Paper updateAccess(Integer uid, Integer vid);

    /**
     * 收藏或取消收藏
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isCollect 是否收藏 true收藏 false取消
     */
    void collectOrCancel(Integer uid, Integer vid, boolean isCollect, Integer fid);
}

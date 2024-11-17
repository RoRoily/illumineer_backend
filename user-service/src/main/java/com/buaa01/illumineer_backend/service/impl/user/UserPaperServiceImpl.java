package com.buaa01.illumineer_backend.service.impl.user;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.User2Paper;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.buaa01.illumineer_backend.mapper.UserPaperMapper;
import com.buaa01.illumineer_backend.service.user.UserPaperService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UserPaperServiceImpl implements UserPaperService {

    @Autowired
    private UserPaperMapper userPaperMapper;

    @Autowired
    private DocumentServiceClient documentServiceClient;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 更新访问次数以及最近播放时间，顺便返回记录信息，没有记录则创建新记录
     * @param uid   文章ID
     * @param pid   文章ID
     * @return 更新后的数据信息
     */
    @Override
    public User2Paper updateAccess(Integer uid, Integer pid) {
        QueryWrapper<User2Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("pid", pid);
        User2Paper userPaper = userPaperMapper.selectOne(queryWrapper);
        if (userPaper == null) {
            // 记录不存在，创建新记录
            userPaper = new User2Paper(null, uid, pid, 0, new Date());
            userPaperMapper.insert(userPaper);
        } else if (System.currentTimeMillis() - userPaper.getAcessDate().getTime() <= 30000) {
            // 如果最近30秒内播放过则不更新记录，直接返回
            userPaper.updateWeight(1); // 浏览+1
            return userPaper;
        } else {
            userPaper.setAcessDate(new Date());
            userPaper.updateWeight(1); // 浏览+1
            userPaperMapper.updateById(userPaper);
        }
        // 异步线程更新video表和redis
        CompletableFuture.runAsync(() -> {
            redisTool.storeZSet("user_video_history:" + uid, vid);   // 添加到/更新观看历史记录
            documentServiceClient.updateVideoStatus(vid, "play", true, 1);
        }, taskExecutor);
        return userPaper;
    }

    /**
     * 收藏或取消收藏
     * @param uid   用户ID
     * @param pid   文章ID
     * @param isCollect 是否收藏 true收藏 false取消
     * @return  返回更新后的信息
     */
    @Override
    public void collectOrCancel(Integer uid, Integer pid, boolean isCollect) {
        UpdateWrapper<User2Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).eq("pid", pid);
        if (isCollect) {
            updateWrapper.setSql("collect = 1, weight = weight + 3"); // 收藏+3
        } else {
            updateWrapper.setSql("collect = 0");
        }
        CompletableFuture.runAsync(() -> {
            videoServiceClient.updateVideoStatus(pid, "collect", isCollect, 1);
        }, taskExecutor);
        userPaperMapper.update(null, updateWrapper);
    }
}

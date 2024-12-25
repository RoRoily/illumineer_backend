package com.buaa01.illumineer_backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.Paper;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.entity.User2Paper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.mapper.UserPaperMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.user.UserFavoriteService;
import com.buaa01.illumineer_backend.service.user.UserPaperService;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UserPaperServiceImpl implements UserPaperService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PaperServiceClient paperServiceClient;

    @Autowired
    private UserPaperMapper userPaperMapper;

    @Autowired
    private UserFavoriteService userFavoriteService;

    // @Autowired
    // private DocumentServiceClient documentServiceClient;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 更新访问次数以及最近访问时间，顺便返回记录信息，没有记录则创建新记录
     *
     * @param uid 用户ID
     * @param pid 文章ID
     * @return 更新后的数据信息
     */
    @Override
    public User2Paper updateAccess(Integer uid, Long pid) {
        QueryWrapper<User2Paper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("pid", pid);
        User2Paper userPaper = userPaperMapper.selectOne(queryWrapper);
        if (userPaper == null) {
            // 记录不存在，创建新记录
            userPaper = new User2Paper(null, uid, pid, 0, new Date());
            userPaperMapper.insert(userPaper);
            // 修改权重
            updateIntention(uid, pid, 1); // 浏览 +1
        } else {
            userPaper.setAcessDate(new Date());
            userPaperMapper.updateById(userPaper);
        }
        String hisKey = "uForHis" + uid;
        // 异步线程更新video表和redis
        CompletableFuture.runAsync(() -> {
            redisTool.addSetMember(hisKey, pid);
            paperServiceClient.updatePaperStatus(pid, "play", true, 1);
        }, taskExecutor);
        return userPaper;
    }

    /**
     * 收藏或取消收藏
     *
     * @param uid       用户ID
     * @param pid       文章ID
     * @param isCollect 是否收藏 true收藏 false取消
     * @return 返回更新后的信息
     */
    @Override
    public void collectOrCancel(Integer uid, Long pid, boolean isCollect, Integer fid) {
        UpdateWrapper<User2Paper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).eq("pid", pid);
        if (isCollect) {
            updateWrapper.setSql("collect = 1");
            updateIntention(uid, pid, 3); // 收藏 +3
            // 进行收藏操作
            // userFavoriteService.addPapertoFav(fid, pid);
        } else {
            updateWrapper.setSql("collect = 0");
            // 取消收藏
        }
        CompletableFuture.runAsync(() -> {
            paperServiceClient.updatePaperStatus(pid, "collect", isCollect, 1);
        }, taskExecutor);
        userPaperMapper.update(null, updateWrapper);
    }

    // 增加相应cid的权重
    private void updateIntention(Integer uid, Long pid, Integer addWeight) {
        Integer cid = getCid(pid);
        User user = getUser(uid);

        // 获取最新的weight
        Map<Category, Integer> intention = user.getIntention();
        for (Category category : intention.keySet()) {
            if (Objects.equals(Integer.parseInt(category.getSubClassId()), cid)) {
                Integer weight = intention.get(category);
                intention.put(category, weight + addWeight);
                break;
            }
        }

        // 更新weight
        userMapper.updateCategory(uid, intention);
    }

    // 根据pid获得cid
    private int getCid(Long pid) {
        // 根据pid找category
        Paper paper = getPaper(pid);

        return paper.getCategoryId();
    }

    private Paper getPaper(Long pid) {
        // Paper paper = null;
        // QueryWrapper<Paper> paperQueryWrapper = new QueryWrapper<>();
        // paperQueryWrapper.eq("pid", pid);
        // paper = paperMapper.selectOne(paperQueryWrapper);
        return (Paper) paperServiceClient.getPaperById(pid);
    }

    private User getUser(Integer uid) {
        User user = null;
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("uid", uid);
        user = userMapper.selectOne(userQueryWrapper);
        return user;
    }
}
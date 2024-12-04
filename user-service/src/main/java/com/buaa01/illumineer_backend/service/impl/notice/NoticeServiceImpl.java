package com.buaa01.illumineer_backend.service.impl.notice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Notice;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.im.handler.NoticeHandler;
import com.buaa01.illumineer_backend.mapper.NoticeMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.notice.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    /**
     * 向指定用户发送消息
     *
     * @param uid     用户id
     * @param title   消息标题
     * @param content 消息内容
     * @return 响应对象
     */
    @Override
    public CustomResponse sendNotice(Integer uid, String title, String content) {
        CustomResponse response = new CustomResponse();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            response.setCode(404);
            response.setMessage("找不到对应的用户");
            return response;
        }
        //创建一条消息
        Notice notice = new Notice(null,uid,title,content);
        noticeMapper.insert(notice);
        NoticeHandler.sendNotice(notice);
        return response;
    }

    /**
     * 获取指定User的消息
     *
     * @param uid 用户id
     * @return Notice数组
     */
    @Override
    public List<Notice> getNotice(Integer uid) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        return noticeMapper.selectList(queryWrapper);
    }
}

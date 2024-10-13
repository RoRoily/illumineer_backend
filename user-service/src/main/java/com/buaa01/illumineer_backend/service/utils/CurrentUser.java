package com.buaa01.illumineer_backend.service.utils;


import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.bilimili.buaa13.service.impl.user.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CurrentUser {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 获取当前登录用户的uid，也是JWT认证的一环
     * @return 当前登录用户的uid
     */
    public Integer getUserId() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User suser = loginUser.getUser();   // 这里的user是登录时存的security:user，因为是静态数据，可能会跟实际的有区别，所以只能用作获取uid用
        return suser.getUid();
    }

    /**
     * 判断当前用户是否管理员
     * @return  是否管理员 true则为管理员/false则不是
     */
    public Boolean isAdmin() {
        Integer uid = getUserId();
        User user = userMapper.selectById(uid);
        return user.getStatus() == 0;
    }

    /**
     * 获取当前用户
     * @return User
     */
    public User getUser() {
        Integer uid = getUserId();
        return userMapper.selectById(uid);
    }

    /**
     *获取当前登录用户的uid
     * */
    public Integer getUserUid(){
        AtomicReference<UsernamePasswordAuthenticationToken> authenticationToken = new AtomicReference<>((UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication());
        AtomicReference<User> sUser = new AtomicReference<>(new User());
        CompletableFuture<?> futureUid = CompletableFuture.runAsync(()->{
            authenticationToken.set((UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication());
            UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.get().getPrincipal();
            sUser.set(loginUser.getUser());   // 这里的user是登录时存的security:user，因为是静态数据，可能会跟实际的有区别，所以只能用作获取uid用
        },taskExecutor);
        CompletableFuture<?> futureUser = CompletableFuture.runAsync(()->{
            sUser.set((User) authenticationToken.get().getPrincipal());
        },taskExecutor);
        CompletableFuture.allOf(futureUid, futureUser).join();
        return sUser.get().getUid();
    }
}

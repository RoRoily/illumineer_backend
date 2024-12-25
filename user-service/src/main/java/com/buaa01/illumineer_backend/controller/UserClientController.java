package com.buaa01.illumineer_backend.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.FavoriteMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.user.UserAuthService;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import io.netty.channel.Channel;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserClientController {

    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private UserService userService;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private RedisTool redisTool;
//    @Autowired
//    private IMServer imServer;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAuthService userAuthService;


    //从userService中寻找提供的服务
    @GetMapping("/user/{uid}")
    @SentinelResource(value = "getUserById",blockHandler = "getUserByIdHandler")
    public User getUserById(@PathVariable("uid") Integer uid){
        return userService.getUserByUId(uid);
    }

    @PostMapping("/user/currentUser/getId")
    @SentinelResource(value = "getCurrentUserId",blockHandler = "getCurrentUserIdHandler")
    public Integer getCurrentUserId(){
        return currentUser.getUserId();
    }
    public Integer getCurrentUserIdHandler(BlockException exception){return 1;}

    @GetMapping("/user/currentUser")
    public User getCurrentUser(){
        return currentUser.getUser();
    }
    @PostMapping("/user/currentUser/isAdmin")
    @SentinelResource(value = "currentIsAdmin",blockHandler = "currentIsAdminHandler")
    public Boolean currentIsAdmin(){
        return currentUser.isAdmin();
    }
    public Boolean getCurrentIsAdminHandler(BlockException exception){return false;}


    public ResponseEntity<Void> updateFavoriteVideoHandler(@RequestBody List<Map<String, Object>> result, @RequestParam("fid") Integer fid,BlockException exception) {
        return ResponseEntity.badRequest().build();
    }

    public void handleCommentHandler(@RequestParam("uid") Integer uid,
                                     @RequestParam("toUid") Integer toUid,
                                     @RequestParam("id") Integer id,
                                     BlockException exception){
        System.out.println("commentService fallback" + uid+" "+toUid+" "+id);
    }

    /**
     * 更新用户实名下的论文
     * @param pids 需要更新的论文识别码 add 1为添加论文，0为删除论文
     * **/
    @PostMapping("/user/set/authPaper")
    @SentinelResource(value = "setAuthPaper",blockHandler = "setAuthPaperHandler")
    public CustomResponse setAuthPaper(@RequestParam("add") Integer add ,
                                      @RequestParam("pids") String pids){
        CustomResponse customResponse = new CustomResponse();
        List<Long> paperList = new ArrayList<>();
        List<String> papers = List.of(pids.split(","));
        for (String s : papers) {
            try {
                paperList.add(Long.parseLong(s));
            } catch (NumberFormatException e) {
                // 处理可能的转换异常
                System.err.println("Invalid number format: " + s);
            }
        }
        return userAuthService.claim(add,paperList);
    }


    public CustomResponse setFavoriteHandler(@RequestParam("add") Integer add ,
                                             @RequestParam("pids") String pids,
                                             BlockException exception){
        return new CustomResponse(404,"favorite fallback", null);
    }



    @GetMapping("/user/getUserByName/{account}")
    User getUserByName(@PathVariable("account") String account){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        queryWrapper.ne("state", 2);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            System.out.println("found user by name failed");
            return null;
        }

        return user;
    }


}


package com.buaa01.illumineer_backend.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.DTO.UserDTO;
import com.buaa01.illumineer_backend.entity.IMResponse;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.im.IMServer;
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
    private FavoriteVideoMapper favoriteVideoMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private IMServer imServer;
    @Autowired
    private MessageUnreadService messageUnreadService;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private FavoriteVideoService favoriteVideoService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAuthService userAuthService;


    //从userService中寻找提供的服务
    @GetMapping("/user/{uid}")
    @SentinelResource(value = "getUserById",blockHandler = "getUserByIdHandler")
    public UserDTO getUserById(@PathVariable("uid") Integer uid){
        return userService.getUserByUId(uid);
    }
    public UserDTO getUserByIdHandler (@PathVariable("uid") Integer uid, BlockException exception){
        return new UserDTO();
    }

    @PostMapping("/user/currentUser/getId")
    @SentinelResource(value = "getCurrentUserId",blockHandler = "getCurrentUserIdHandler")
    public Integer getCurrentUserId(){
        return currentUser.getUserId();
    }
    public Integer getCurrentUserIdHandler(BlockException exception){return 1;}


    @PostMapping("/user/currentUser/isAdmin")
    @SentinelResource(value = "currentIsAdmin",blockHandler = "currentIsAdminHandler")
    public Boolean currentIsAdmin(){
        return currentUser.isAdmin();
    }
    public Boolean getCurrentIsAdminHandler(BlockException exception){return false;}

    @PostMapping("/user/updateFavoriteVideo")
    @SentinelResource(value = "updateFavoriteVideo", blockHandler = "updateFavoriteVideoHandler")
    public ResponseEntity<Void> updateFavoriteVideo(@RequestBody List<Map<String, Object>> result, @RequestParam("fid") Integer fid) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            result.stream().parallel().forEach(map -> {
                Video video = (Video) map.get("video");
                QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", video.getVid()).eq("fid", fid);
                map.put("info", favoriteVideoMapper.selectOne(queryWrapper));
            });
            sqlSession.commit();
        }
        return ResponseEntity.ok().build();
    }
    public ResponseEntity<Void> updateFavoriteVideoHandler(@RequestBody List<Map<String, Object>> result, @RequestParam("fid") Integer fid,BlockException exception) {
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/user/handle_comment")
    @SentinelResource(value = "handleComment",blockHandler = "handleCommentHandler")
    public void handleComment(@RequestParam("uid") Integer uid,
                              @RequestParam("toUid") Integer toUid,
                              @RequestParam("id") Integer id){

        if(!toUid.equals(uid)) {
            //1注释Redis
            redisTool.storeZSet("reply_zset:" + toUid, id);
            messageUnreadService.addOneUnread(toUid, "reply");

            // 通知未读消息
            Map<String, Object> map = new HashMap<>();
            map.put("type", "接收");
            Set<Channel> commentChannel = IMServer.userChannel.get(toUid);
            if (commentChannel != null) {
                commentChannel.stream().parallel().forEach(channel -> channel.writeAndFlush(IMResponse.message("reply", map)));
            }
        }
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
        List<Integer> paperList = new ArrayList<>();
        List<String> papers = List.of(pids.split(","));
        for (String s : papers) {
            try {
                paperList.add(Integer.parseInt(s));
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

    @GetMapping("/user/getVidsByFid/{fid}")
    List<Integer> getVidsByFid(@PathVariable("fid") Integer fid){
        return favoriteVideoMapper.getVidByFid(fid);
    }

    @GetMapping("/user/getTimeByFid/{fid}")
    List<Date> getTimeByFid(@PathVariable("fid") Integer fid){
        return favoriteVideoMapper.getTimeByFid(fid);
    }
}


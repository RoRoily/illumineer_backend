package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.Category;
import com.buaa01.illumineer_backend.entity.DTO.UserDTO;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CurrentUser currentUser;
    @Qualifier("taskExecutor")
    @Autowired
    private Executor taskExecutor;
    @Autowired
    private PaperServiceClient paperServiceClient;

    /**
     * 根据用户ID获取用户信息
     *
     * @param uid 用户ID
     * @return 用户信息
     */
    @Override
    public User getUserByUId(Integer uid) {
        // 从redis中获取最新数据
        User user = redisTool.getObjectByClass("user" + uid, User.class);
        // 如果redis中没有user数据，就从mysql中获取并更新到redis
        if (user == null) {
            user = userMapper.selectById(uid);
            User finalUser = user;
            CompletableFuture.runAsync(() -> {
                redisTool.setExObjectValue("user:" + finalUser.getUid(), finalUser);  // 默认存活1小时
            }, taskExecutor);
        }
        return user;
    }

    /**
     * 获取用户展示信息
     *
     * @param uid 用户ID
     * @return 用户个人主页信息
     */

    @Override
    public UserDTO getUserHomeInfo(Integer uid) {
        User user = redisTool.getObjectByClass("user:" + uid, User.class);
        if (user == null) {
            user = userMapper.selectById(uid);
            User finalUser = user;
            CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
        }
        return new UserDTO(user);
    }

    /**
     * 获取用户个人简历信息
     *
     * @param uid 用户ID
     * @return 用户个人简历信息
     */

    @Override
    public Map<String, Object> getUserResume(Integer uid) {
        User user = redisTool.getObjectByClass("user:" + uid, User.class);
        if (user == null) {
            user = userMapper.selectById(uid);
            User finalUser = user;
            CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
        }
        Map<String, Object> userResume = new HashMap<>();
        userResume.put("name", user.getName());
        userResume.put("description", user.getDescription());
        userResume.put("institution", user.getInstitution());
        String key = "user_pages_" + uid;
        long offset = redisTool.getSetSize(key);
        Set<Object> set = redisTool.zRange(key, 0, offset);
        userResume.put("field", set);
        return userResume;
    }

    /**
     * 更新用户个人信息
     *
     * @param info 新的用户个人信息
     * @return 更新结果
     */
    @Override
    public int updateUserInfo(Map<String, Object> info) {
        Integer loginUserId = currentUser.getUserId();
        User user = redisTool.getObjectByClass("user:" + loginUserId, User.class);
        if (user == null)
            user = userMapper.selectById(loginUserId);
        String nick_name = (String) info.get("username");
        String name = (String) info.get("name");
        if(nick_name!=null){
            String email = (String) info.get("email");
            String institution = (String) info.get("institution");
            user.setNickName(nick_name);
            user.setEmail(email);
            user.setInstitution(institution);
            if (userMapper.getUserByNickName(nick_name) != null)
                return -1;
            if (userMapper.getUserByEmail(email) != null)
                return -2;
        }//个人主页
        else if(name!=null){
            int gender = (int) info.get("gender");
            String description = (String) info.get("description");
//        List<String> category_ids = List.of(((String) info.get("category")).split(","));
//        List<String> category = paperServiceClient.getCategory(category_ids);
            List<String> category = List.of(((String) info.get("category")).split(","));
            user.setName(name);
            user.setGender(gender);
            user.setDescription(description);
            user.setField(category);
        }//学者主页
        userMapper.updateById(user);
        User finalUser = user;
        CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
        return 0;
    }

    /**
     * 删除用户
     *
     * @return 删除结果
     */

    @Override
    public int deleteUser() {
        Integer loginUserId = currentUser.getUserId();
        User user = redisTool.getObjectByClass("user:" + loginUserId, User.class);
        if (user != null) {
            redisTool.deleteKey("user" + loginUserId);
        }
        User user1 = new User();
        user1.setUid(loginUserId);
        user1.setStatus(2);
        return userMapper.updateById(user);
    }

    /**
     * 封禁用户
     *
     * @param uid 用户ID
     * @return 封禁结果
     */

    @Override
    public int disableUser(Integer uid) {
        User user = new User();
        user.setUid(uid);
        user.setStats(1);
        return userMapper.updateById(user);
    }

    /**
     * 解封用户
     *
     * @param uid 用户ID
     * @return 解封结果
     */
    @Override
    public int ableUser(Integer uid) {
        User user = new User();
        user.setUid(uid);
        user.setStats(0);
        return userMapper.updateById(user);
    }

    @Override
    public void modifyAuthInfo(String name, String institutionName, String address) {
        Integer loginUserId = currentUser.getUserId();
        User user = redisTool.getObjectByClass("user:" + loginUserId, User.class);
        if (user == null)
            user = userMapper.selectById(loginUserId);
        user.setName(name);
        user.setInstitution(institutionName);
        userMapper.updateById(user);
        User finalUser = user;
        CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
    }
}

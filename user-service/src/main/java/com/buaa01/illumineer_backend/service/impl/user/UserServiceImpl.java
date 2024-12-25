package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.DTO.UserDTO;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.client.PaperServiceClient;
import com.buaa01.illumineer_backend.service.user.UserService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;

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
                redisTool.setExObjectValue("user:" + finalUser.getUid(), finalUser); // 默认存活1小时
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
        //User user = redisTool.getObjectByClass("user:" + uid, User.class);
        //if (user == null) {
            User user = userMapper.selectById(uid);
            User finalUser = user;
            CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
       // }
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
        User user = redisTool.getObjectByClass("user" + loginUserId, User.class);
        if (user == null)
            user = userMapper.selectById(loginUserId);
        String old_name = user.getNickName();
        String old_email = user.getEmail();
        if (info.get("username") != null) {
            String nick_name = (String) info.get("username");
            if (userMapper.getUserByNickName(nick_name) != null && !old_name.equals(nick_name))
                return -1;
            user.setNickName(nick_name);
        }
        if (info.get("name") != null) {
            String name = (String) info.get("name");
            user.setName(name);
        }
        if (info.get("email") != null) {
            String email = (String) info.get("email");
            if (userMapper.getUserByEmail(email) != null && !old_email.equals(email))
                return -2;
            user.setEmail(email);
        }
        if (info.get("institution") != null) {
            String institution = (String) info.get("institution");
            user.setInstitution(institution);
        }
        if (info.get("gender") != null) {
            int gender = (int) info.get("gender");
            user.setGender(gender);
        }
        if (info.get("description") != null) {
            String description = (String) info.get("description");
            user.setDescription(description);
        }
        if (info.get("category") != null) {
            // List<String> category_ids = List.of(((String)
            // info.get("category")).split(","));
            // List<String> category = paperServiceClient.getCategory(category_ids);
            List<String> category = List.of(((String) info.get("category")).split(","));
            user.setField(category);
        }
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
        user.setIsVerify(true);
        userMapper.updateById(user);
        User finalUser = user;
        CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
    }

    @Override
    public void modifyAuthInfoWithRedis(String name, String institutionName, String address) {
        Integer loginUserId = redisTool.getObjectByClass("orcid", Integer.class);
        User user = redisTool.getObjectByClass("user:" + loginUserId, User.class);
        if (user == null)
            user = userMapper.selectById(loginUserId);
        user.setName(name);
        user.setInstitution(institutionName);
        user.setIsVerify(true);
        userMapper.updateById(user);
        User finalUser = user;
        CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" + finalUser.getUid(), finalUser));
    }

    /*
     * UserFavBias 自增
     */
    @Override
    public void updataUserFavBias() {
        User user = userMapper.selectById(currentUser.getUserId());
        user.setFavBias(user.getFavBias() + 1);

        userMapper.updateById(user);
        CompletableFuture.runAsync(() -> redisTool.setObjectValue("user:" +
                user.getUid(), user));
    }
}

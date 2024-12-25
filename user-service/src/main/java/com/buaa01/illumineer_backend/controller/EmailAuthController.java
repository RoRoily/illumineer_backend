package com.buaa01.illumineer_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.Institution;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.mapper.InstitutionMapper;
import com.buaa01.illumineer_backend.mapper.UserMapper;
import com.buaa01.illumineer_backend.service.email.EmailService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.UUID;

/**
 * 用户邮箱认证流程
 * **/
@RestController
@RequestMapping("/auth")
public class EmailAuthController {

    //用户数据存储交互和发送邮件
    //private final UserRepository userRepository;

    @Value("${verification.url}")
    private String verificationUrl;

    @Autowired
    private EmailService emailService;
    @Autowired
    private InstitutionMapper institutionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private CurrentUser currentUser;


    @PostMapping("/register")
    public CustomResponse register(@RequestParam("email") String email) {
        CustomResponse customResponse = new CustomResponse();
        // 1. 提取邮箱的域名部分
        String domain = email.substring(email.indexOf('@') + 1);
        //System.out.println(domain);
        // 2. 根据域名查询机构
        QueryWrapper<Institution> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("domain", domain);
        Institution institution = institutionMapper.selectOne(queryWrapper);
        if(institution == null){
            customResponse.setData(500);
            customResponse.setMessage("unknown academic institution");
            return customResponse;
        }

        // 2. 生成认证 token 并保存用户信息(uid和认证机构)
        Integer verifyUid = currentUser.getUserId();
        String token = UUID.randomUUID().toString();
        String institutionToken = "institution:" +token;
        redisTool.setExValue(token,verifyUid);
        redisTool.setExValue(institutionToken,institution);

        //User user = new User(email, "北航", false, token, expiry);
        //userRepository.save(user);

        // 3. 发送认证邮件
        try {
            emailService.sendVerificationEmail(email, token, verificationUrl,verifyUid);
            customResponse.setCode(200);
            customResponse.setMessage("Email send,please check ^_^");
            customResponse.setData(token);
            return customResponse;
        } catch (MessagingException e) {
            customResponse.setMessage("send failed");
            customResponse.setCode(500);
            return customResponse;
        }
    }

    @GetMapping("/verify")
    public CustomResponse verifyEmail(@RequestParam("token") String token) {
        CustomResponse customResponse = new CustomResponse();
        // 1.通过请求中的 token（用户在邮件中点击的链接中的参数），从数据库中查找对应的用户。
        //Optional<User> userOptional = userRepository.findByVerificationToken(token);
        Integer verifyUid =(Integer) redisTool.getValue(token);
        String institutionToken = "institution:" +token;
        Institution verifiedInstitution = (Institution)redisTool.getValue(institutionToken);

        if(verifyUid==null){
            customResponse.setCode(500);
            customResponse.setMessage("Invalid Token");
            return customResponse;
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("uid",verifyUid);
        User user = userMapper.selectOne(userQueryWrapper);
        if(user == null){
            customResponse.setCode(500);
            customResponse.setMessage("Invalid User");
            return customResponse;
        }
        user.setIsVerify(true);
        user.setInstitution(verifiedInstitution.getName());
        // 2. 更新用户认证状态
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("uid",user.getUid());
        userUpdateWrapper.set("is_verify",true);
        userUpdateWrapper.set("institution",verifiedInstitution.getName());
        userMapper.update(null,userUpdateWrapper);
        customResponse.setCode(200);
        customResponse.setMessage("Email Verified");
        return customResponse;
    }
}


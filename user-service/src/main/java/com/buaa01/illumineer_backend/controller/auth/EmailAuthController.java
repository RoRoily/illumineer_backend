package com.buaa01.illumineer_backend.controller.auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.buaa01.illumineer_backend.service.email.EmailService;
import com.buaa01.illumineer_backend.entity.Institution;
import com.buaa01.illumineer_backend.entity.User;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户邮箱认证流程
 * **/
@RestController
@RequestMapping("/auth")
public class EmailAuthController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${verification.url}")
    private String verificationUrl;

    public EmailAuthController(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String email) {
        // 1. 提取邮箱的域名部分
        String domain = email.substring(email.indexOf('@') + 1);

        // 2. 根据域名查询机构
        Optional<Institution> institutionOpt = institutionRepository.findByDomain(domain);
        if (institutionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("不支持的机构邮箱");
        }

        String institutionName = institutionOpt.get().getName();

        //TODO: 更新用户中的Institution name

        // 2. 生成认证 token 并保存用户信息
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        User user = new User(email, "北航", false, token, expiry);
        userRepository.save(user);

        // 3. 发送认证邮件
        try {
            emailService.sendVerificationEmail(email, token, verificationUrl);
            return ResponseEntity.ok("认证邮件已发送，请查收");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("发送邮件失败");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        // 1. 查找用户并验证 token
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("无效的认证链接");
        }

        User user = userOptional.get();
        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("认证链接已过期");
        }

        // 2. 更新用户认证状态
        user.setVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok("邮箱认证成功");
    }
}


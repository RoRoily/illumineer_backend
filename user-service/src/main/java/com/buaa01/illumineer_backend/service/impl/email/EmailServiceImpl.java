package com.buaa01.illumineer_backend.service.impl.email;

import com.buaa01.illumineer_backend.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String mailFrom;

    public void EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token, String verificationUrl, Integer uid) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String link = verificationUrl + uid +"/authenticationInfo?token=" + token + "&email=" + to;
        String content = "<p>点击以下链接完成认证：</p>" +
                "<a href=\"" + link + "\">完成认证</a>";

        helper.setFrom(mailFrom);
        helper.setTo(to);
        helper.setSubject("邮箱认证");
        helper.setText(content, true);

        mailSender.send(message);
    }
}

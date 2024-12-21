package com.buaa01.illumineer_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    //@Value("${spring.mail.host}")
    private final String mailHost = "smtp.163.com";

    //@Value("${spring.mail.port}")
    private final int mailPort = 25;

    //@Value("${spring.mail.username}")
    private final String mailUsername = "XinyangPengbuaa@163.com";

    //@Value("${spring.mail.password}")
    private final String mailPassword = "JQyYDwCv2ecf32jW";

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost); // 设置 SMTP 主机
        mailSender.setPort(mailPort); // 设置端口
        mailSender.setUsername(mailUsername); // 设置用户名
        mailSender.setPassword(mailPassword); // 设置密码

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }
}

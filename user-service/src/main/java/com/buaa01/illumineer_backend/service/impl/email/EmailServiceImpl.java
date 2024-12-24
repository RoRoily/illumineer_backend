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

        String link = "http://" + verificationUrl + uid +"/authenticationInfo?token=" + token + "&email=" + to;
        String content = "<html>" +
                "<body>" +
                "<p>Please click the following link to complete verification:</p>" +
                "<a href=\"" + link + "\" target=\"_blank\">" +
                "Complete Verification</a>" +
                "</body>" +
                "</html>";

        helper.setFrom(mailFrom);
        helper.setTo(to);
        helper.setSubject("Check Email");
        helper.setText(content, true);

        mailSender.send(message);
    }
}

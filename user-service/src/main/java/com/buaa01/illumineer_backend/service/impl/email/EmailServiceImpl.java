import com.buaa01.illumineer_backend.service.email.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Overide
    public void sendVerificationEmail(String to, String token, String verificationUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String link = verificationUrl + "?token=" + token;
        String content = "<p>点击以下链接完成认证：</p>" +
                "<a href=\"" + link + "\">完成认证</a>";

        helper.setTo(to);
        helper.setSubject("邮箱认证");
        helper.setText(content, true);

        mailSender.send(message);
    }
}

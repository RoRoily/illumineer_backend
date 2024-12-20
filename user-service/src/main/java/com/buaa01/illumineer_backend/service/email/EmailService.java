package com.buaa01.illumineer_backend.service.email;

import javax.mail.MessagingException;

public interface EmailService {
    public void sendVerificationEmail(String to, String token, String verificationUrl, Integer uid) throws MessagingException;
}

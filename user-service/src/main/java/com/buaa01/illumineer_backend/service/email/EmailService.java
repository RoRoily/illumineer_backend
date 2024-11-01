package com.buaa01.illumineer_backend.service.email;

public interface EmailService {
    public void sendVerificationEmail(String to, String token, String verificationUrl) throws MessagingException ;
}

package com.example.demo;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class EmailOtpService {

    private static final Duration OTP_LIFETIME =
            Duration.ofMinutes(10);

    private static final int MAX_ATTEMPTS = 5;

    private final JavaMailSender mailSender;
    private final PasswordEncoder encoder;

    private final SecureRandom random = new SecureRandom();

    public EmailOtpService(JavaMailSender mailSender,
                           PasswordEncoder encoder) {
        this.mailSender = mailSender;
        this.encoder = encoder;
    }

    public String generateOtp() {
        return String.format("%06d",
                random.nextInt(1_000_000));
    }

    public Instant expiresAt() {
        return Instant.now().plus(OTP_LIFETIME);
    }

    public String hash(String otp) {
        return encoder.encode(otp);
    }

    public boolean verify(String otp,
                          String otpHash,
                          int attempts) {

        return attempts < MAX_ATTEMPTS
                && encoder.matches(otp, otpHash);
    }

    public boolean expired(Instant expiresAt) {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean tooManyAttempts(int attempts) {
        return attempts >= MAX_ATTEMPTS;
    }

    public void sendRegistrationOtp(String email,
                                    String otp) {

        send(
            email,
            "I-ITSM registration verification code",
            "Your I-ITSM registration OTP is: "
                    + otp
                    + "\n\nThis code expires in 10 minutes."
        );
    }

    public void sendPasswordResetOtp(String email,
                                     String otp) {

        send(
            email,
            "I-ITSM password reset code",
            "Your I-ITSM password reset OTP is: "
                    + otp
                    + "\n\nThis code expires in 10 minutes."
        );
    }

    private void send(String email,
                      String subject,
                      String text) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}

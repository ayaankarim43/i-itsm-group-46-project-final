package com.example.demo;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_challenges")
public class PasswordResetChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private boolean used;

    protected PasswordResetChallenge() {}

    public PasswordResetChallenge(
            String email,
            String otpHash,
            Instant expiresAt) {

        this.email = email;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public boolean isUsed() {
        return used;
    }

    public void incrementAttempts() {
        attempts++;
    }

    public void markUsed() {
        used = true;
    }
}

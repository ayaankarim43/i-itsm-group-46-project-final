package com.example.demo;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "pending_registrations")
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts;

    protected PendingRegistration() {}

    public PendingRegistration(
            String name,
            String email,
            String department,
            String passwordHash,
            String otpHash,
            Instant expiresAt) {

        this.name = name;
        this.email = email;
        this.department = department;
        this.passwordHash = passwordHash;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getPasswordHash() {
        return passwordHash;
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

    public void incrementAttempts() {
        attempts++;
    }
}

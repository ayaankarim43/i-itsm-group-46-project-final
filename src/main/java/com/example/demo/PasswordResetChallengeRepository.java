package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetChallengeRepository
        extends JpaRepository<PasswordResetChallenge, Long> {

    Optional<PasswordResetChallenge>
    findTopByEmailOrderByIdDesc(String email);

    @Transactional
    void deleteByEmail(String email);
}

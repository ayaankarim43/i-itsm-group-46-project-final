package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginFailureHandler
        implements AuthenticationFailureHandler {

    private final UserRepository users;

    public LoginFailureHandler(UserRepository users) {
        this.users = users;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {

        String email =
                request.getParameter("username");

        boolean registered =
                email != null
                && !email.isBlank()
                && users.findByEmail(
                        email.trim().toLowerCase())
                    .isPresent();

        String error =
                registered
                ? "invalid"
                : "unregistered";

        response.sendRedirect(
                "/login?error="
                + URLEncoder.encode(
                    error,
                    StandardCharsets.UTF_8));
    }
}

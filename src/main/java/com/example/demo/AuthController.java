package com.example.demo;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @GetMapping("/me")
    Map<String, Object> me(Authentication a) {

        return Map.of(
            "email", a.getName(),
            "roles",
            a.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
        );
    }
}
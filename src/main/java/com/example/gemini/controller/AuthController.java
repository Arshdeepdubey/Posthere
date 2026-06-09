package com.example.gemini.controller;

import com.example.gemini.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/generate-token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest tokenRequest) {
        String token = jwtTokenProvider.generateToken(tokenRequest.getUsername());
        return ResponseEntity.ok(new TokenResponse(token, "Token generated successfully"));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ValidationResponse> validateToken(@RequestBody ValidateTokenRequest request) {
        boolean isValid = jwtTokenProvider.validateToken(request.getToken()) && !jwtTokenProvider.isTokenExpired(request.getToken());
        String username = isValid ? jwtTokenProvider.getUsernameFromToken(request.getToken()) : null;
        return ResponseEntity.ok(new ValidationResponse(isValid, username, isValid ? "Token is valid" : "Token is invalid or expired"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRequest {
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateTokenRequest {
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenResponse {
        private String token;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResponse {
        private boolean valid;
        private String username;
        private String message;
    }
}

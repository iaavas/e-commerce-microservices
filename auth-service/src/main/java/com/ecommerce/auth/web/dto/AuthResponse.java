package com.ecommerce.auth.web.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {
}

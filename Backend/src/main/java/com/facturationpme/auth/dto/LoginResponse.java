package com.facturationpme.auth.dto;

import com.facturationpme.users.dto.UserResponse;

/**
 * Aligne sur {@code ILoginResponse} (core/models/auth.model.ts) - meme forme pour login ET refresh.
 */
public record LoginResponse(
    String accessToken, String refreshToken, long expiresIn, UserResponse user) {}

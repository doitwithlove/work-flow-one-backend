package com.touchmind.work.flow.one.dto;

public record LoginResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresIn

) {
}

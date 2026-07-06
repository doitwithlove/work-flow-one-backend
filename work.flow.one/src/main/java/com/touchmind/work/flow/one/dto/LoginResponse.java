package com.touchmind.work.flow.one.dto;

import java.util.Set;

public record LoginResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresIn,

        String userId,

        String username,

        Set<String> roles

) {
}

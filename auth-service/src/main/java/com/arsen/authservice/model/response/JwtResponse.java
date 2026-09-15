package com.arsen.authservice.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String refreshToken;
    private Long expiresIn;
}

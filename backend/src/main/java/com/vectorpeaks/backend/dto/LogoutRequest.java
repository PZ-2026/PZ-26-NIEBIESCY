package com.vectorpeaks.backend.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
    private Integer userId;
    private String fcmToken;
}
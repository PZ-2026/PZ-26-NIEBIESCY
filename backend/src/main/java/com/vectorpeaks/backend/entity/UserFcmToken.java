package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_fcm_tokens")
public class UserFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 255)
    private String fcmToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // getters & setters
    public Integer getId()        { return id; }
    public Integer getUserId()    { return userId; }
    public String getFcmToken()   { return fcmToken; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUserId(Integer userId)     { this.userId = userId; }
    public void setFcmToken(String fcmToken)  { this.fcmToken = fcmToken; }
}
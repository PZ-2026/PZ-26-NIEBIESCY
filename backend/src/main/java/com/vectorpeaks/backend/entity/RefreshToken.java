package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a long-lived refresh token stored in the database.
 * Used to issue new access tokens without requiring re-login.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Gettery i settery
    public Integer getId()              { return id; }
    public String getToken()            { return token; }
    public Integer getUserId()          { return userId; }
    public Instant getExpiresAt()       { return expiresAt; }
    public boolean isRevoked()          { return revoked; }
    public Instant getCreatedAt()       { return createdAt; }

    public void setToken(String token){ this.token = token; }
    public void setUserId(Integer userId){ this.userId = userId; }
    public void setExpiresAt(Instant expiresAt){ this.expiresAt = expiresAt; }
    public void setRevoked(boolean revoked){ this.revoked = revoked; }
}
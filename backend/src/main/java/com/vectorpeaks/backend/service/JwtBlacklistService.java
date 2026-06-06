package com.vectorpeaks.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing a local, in-memory JSON Web Token (JWT) blacklist.
 * It tracks revoked tokens and automatically evicts expired ones to manage memory.
 */
@Service
public class JwtBlacklistService {

    // Map: Token -> Expiration time
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    /**
     * Adds a specific JWT token to the blacklist with a defined expiration timestamp.
     *
     * @param token     the JWT token string to be blacklisted
     * @param expiresAt the timestamp when the token naturally expires
     */
    public void blacklistToken(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
    }

    /**
     * Checks whether the provided JWT token is currently present in the blacklist.
     *
     * @param token the JWT token string to verify
     * @return true if the token is blacklisted; false otherwise
     */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Scheduled task that runs every x minutes (from .env file amount of time) to remove expired tokens from the memory.
     * This prevents the blacklist map from growing indefinitely.
     */
    @Scheduled(fixedRateString = "${jwt.blacklist-cleanup-rate}")
    public void cleanUpExpiredTokens() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}

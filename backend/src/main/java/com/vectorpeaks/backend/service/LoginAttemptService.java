package com.vectorpeaks.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-layer brute-force protection.
 *
 * Layer 1 — Lockout based on email+IP pair (prevents account-targeted DoS attacks).
 * Layer 2 — Lockout based on IP only (prevents credential stuffing attacks).
 * Layer 3 — Maps are periodically cleaned up every 10 minutes (OOM prevention).
 *
 * PRODUCTION NOTICE: Replace ConcurrentHashMap with Redis utilizing TTL.
 *
 * @version 1.1
 * @author EduLink Team
 */
@Service
public class LoginAttemptService {

    private static final int  MAX_ATTEMPTS_PER_PAIR = 5;   // email+IP
    private static final int  MAX_ATTEMPTS_PER_IP   = 20;  // IP only (credential stuffing)
    private static final long LOCKOUT_SECONDS        = 900; // 15 minutes
    private static final int  MAX_MAP_SIZE           = 10_000; // OOM guard

    // Key: "email::ip" → number of attempts
    private final Map<String, Integer> attemptsByPair   = new ConcurrentHashMap<>();

    // Key: "email::ip" or "ip" → lockout expiration timestamp
    private final Map<String, Instant> lockouts         = new ConcurrentHashMap<>();

    // Key: "ip" → number of attempts (credential stuffing prevention)
    private final Map<String, Integer> attemptsByIp     = new ConcurrentHashMap<>();

    // ── Public API ────────────────────────────────────────────

    /**
     * Checks if the request is blocked either by email+IP pair or by IP address.
     */
    public boolean isBlocked(String email, String ip) {
        return isKeyBlocked(pairKey(email, ip)) || isKeyBlocked(ip);
    }

    /**
     * Returns the progressive delay in milliseconds to slow down potential attackers.
     */
    public long getDelayMs(String email, String ip) {
        int count = attemptsByPair.getOrDefault(pairKey(email, ip), 0);
        if (count == 3) return 2_000L;
        if (count == 4) return 5_000L;
        return 0L;
    }

    /**
     * Records a failed login attempt across multiple layers.
     */
    public void recordFailure(String email, String ip) {
        String pair = pairKey(email, ip);

        // Layer 1: email+IP protection
        if (attemptsByPair.size() < MAX_MAP_SIZE) {
            int pairCount = attemptsByPair.merge(pair, 1, Integer::sum);
            if (pairCount >= MAX_ATTEMPTS_PER_PAIR) {
                lockouts.put(pair, Instant.now().plusSeconds(LOCKOUT_SECONDS));
                attemptsByPair.remove(pair);
            }
        }

        // Layer 2: IP-only protection (Credential Stuffing guard)
        if (attemptsByIp.size() < MAX_MAP_SIZE) {
            int ipCount = attemptsByIp.merge(ip, 1, Integer::sum);
            if (ipCount >= MAX_ATTEMPTS_PER_IP) {
                lockouts.put(ip, Instant.now().plusSeconds(LOCKOUT_SECONDS));
                attemptsByIp.remove(ip);
            }
        }
    }

    /**
     * Clears the lockout state and attempt counter for the specific email+IP pair upon success.
     */
    public void recordSuccess(String email, String ip) {
        String pair = pairKey(email, ip);
        attemptsByPair.remove(pair);
        lockouts.remove(pair);
        // Note: IP counter is intentionally NOT cleared on success to robustly prevent credential stuffing
    }

    // ── Scheduled Maintenance (OOM Guard) ───────────────────────

    /**
     * Periodically cleans up expired lockouts every 10 minutes.
     */
    @Scheduled(fixedDelay = 600_000)
    public void cleanupExpiredLockouts() {
        Instant now = Instant.now();
        lockouts.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }

    // ── Private Helpers ─────────────────────────────────────────

    private String pairKey(String email, String ip) {
        return email.toLowerCase() + "::" + ip;
    }

    private boolean isKeyBlocked(String key) {
        Instant lockUntil = lockouts.get(key);
        if (lockUntil == null) return false;

        if (Instant.now().isAfter(lockUntil)) {
            lockouts.remove(key);
            return false;
        }
        return true;
    }
}
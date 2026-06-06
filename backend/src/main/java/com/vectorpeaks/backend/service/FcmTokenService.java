package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.UserFcmToken;
import com.vectorpeaks.backend.repository.UserFcmTokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages FCM device tokens for push notifications.
 * Supports multiple devices per user.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Service
public class FcmTokenService {

    private final UserFcmTokenRepository fcmTokenRepository;

    public FcmTokenService(UserFcmTokenRepository fcmTokenRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
    }

    /**
     * Registers an FCM token for a user's device.
     * To prevent duplicate tokens or mixed notifications when a device changes hands
     * or a different user logs in, any existing entry with this token is first removed
     * before saving the new user association.
     *
     * @param userId   the ID of the user registering the token
     * @param fcmToken the Firebase Cloud Messaging token string
     */
    public void registerToken(Integer userId, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) return;

        // Clean up any existing association with this token (ownership update / re-login cleanup)
        fcmTokenRepository.deleteByFcmToken(fcmToken);

        // Save a fresh entry for the currently logged-in user
        UserFcmToken token = new UserFcmToken();
        token.setUserId(userId);
        token.setFcmToken(fcmToken);
        fcmTokenRepository.save(token);
    }

    /**
     * Removes a specific device token on logout.
     * Other devices of the same user remain active.
     */
    public void removeToken(String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        fcmTokenRepository.deleteByFcmToken(fcmToken);
    }

    /**
     * Returns all FCM tokens for a user (all their devices).
     * Used when sending push notifications.
     */
    public List<String> getTokensForUser(Integer userId) {
        return fcmTokenRepository.findByUserId(userId)
                .stream()
                .map(UserFcmToken::getFcmToken)
                .toList();
    }

    /**
     * Removes all tokens for a user — called when account is deleted.
     */
    public void removeAllTokensForUser(Integer userId) {
        fcmTokenRepository.deleteByUserId(userId);
    }
}
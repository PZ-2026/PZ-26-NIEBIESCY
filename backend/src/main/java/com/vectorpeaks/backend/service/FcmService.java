/*
 * FcmService.java
 *
 * Version: 1.1
 * Date: 2026-05-12
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending push notifications via Firebase Cloud Messaging (FCM).
 *
 * <p>Integrates with Firebase Admin SDK to deliver real-time notifications to mobile devices.
 * All notifications are logged for debugging and monitoring purposes.
 *
 * @version 1.1
 * @author EduLink Team
 */
@Service
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    /**
     * Sends a push notification to a specific device identified by its FCM token.
     *
     * <p>The method gracefully handles:
     * <ul>
     *   <li>null or blank tokens (skips sending),</li>
     *   <li>Firebase connectivity issues (logs error and continues),</li>
     *   <li>successful sends (logs message ID for tracing).</li>
     * </ul>
     *
     * @param fcmToken the recipient device's FCM registration token
     * @param title    the notification title (e.g., sender's name)
     * @param body     the notification body text (e.g., message content)
     */
    public void sendNotification(String fcmToken, String title, String body) {
        // Validation: skip if token is missing
        if (fcmToken == null || fcmToken.isBlank()) {
            logger.debug("FCM token is null or blank, skipping notification");
            return;
        }

        // Validation: skip if content is missing
        if (body == null || body.isBlank()) {
            logger.debug("Notification body is empty, skipping");
            return;
        }

        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title != null ? title : "EduLink")
                            .setBody(body)
                            .build())
                    .setToken(fcmToken)
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            logger.info("FCM notification sent successfully. Message ID: {}, Title: {}", messageId, title);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid FCM token: {}. Token may have expired.", fcmToken);
        } catch (Exception e) {
            logger.error("Failed to send FCM notification to token: {}. Error: {}",
                    fcmToken, e.getMessage(), e);
        }
    }
}
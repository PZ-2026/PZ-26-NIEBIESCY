/*
 * FcmTokenServiceTest.java
 *
 * Version: 1.0
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.UserFcmToken;
import com.vectorpeaks.backend.repository.UserFcmTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FcmTokenService}.
 *
 * <p>Verifies the management of FCM device tokens for push notifications, including:
 * <ul>
 * <li>Registering new device tokens safely without duplicates.</li>
 * <li>Handling null or blank token edge cases securely.</li>
 * <li>Retrieving associated tokens for a specific user.</li>
 * <li>Removing individual or all tokens upon logout or account deletion.</li>
 * </ul>
 *
 * @version 1.0
 * @author EduLink Team
 */
@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {

    @Mock
    private UserFcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private FcmTokenService fcmTokenService;

    private final Integer TEST_USER_ID = 1;
    private final String VALID_TOKEN = "fcm-device-token-123xyz";

    @BeforeEach
    void setUp() {
        // Clear setup if needed, Mockito handles repository isolation by default
    }

    /**
     * Test suite focusing on token registration flows.
     */
    @Nested
    @DisplayName("registerToken()")
    class RegisterTokenTests {

        /**
         * Verifies that a completely new token is successfully saved to the database.
         */
        @Test
        @DisplayName("Valid and new token → saves to repository")
        void registerToken_validNewToken_savesToken() {
            // Arrange
            when(fcmTokenRepository.existsByFcmToken(VALID_TOKEN)).thenReturn(false);

            // Act
            fcmTokenService.registerToken(TEST_USER_ID, VALID_TOKEN);

            // Assert
            ArgumentCaptor<UserFcmToken> tokenCaptor = ArgumentCaptor.forClass(UserFcmToken.class);
            verify(fcmTokenRepository, times(1)).save(tokenCaptor.capture());

            UserFcmToken savedToken = tokenCaptor.getValue();
            assertEquals(TEST_USER_ID, savedToken.getUserId());
            assertEquals(VALID_TOKEN, savedToken.getFcmToken());
        }

        /**
         * Confirms that if a token already exists in the system, 
         * the service skips the insertion to prevent database constraints violations or duplicates.
         */
        @Test
        @DisplayName("Token already exists → skips insertion")
        void registerToken_tokenExists_doesNotSave() {
            // Arrange
            when(fcmTokenRepository.existsByFcmToken(VALID_TOKEN)).thenReturn(true);

            // Act
            fcmTokenService.registerToken(TEST_USER_ID, VALID_TOKEN);

            // Assert
            verify(fcmTokenRepository, never()).save(any(UserFcmToken.class));
        }

        /**
         * Ensures that null or empty string tokens are ignored without throwing exceptions.
         */
        @Test
        @DisplayName("Null or blank token → returns immediately without actions")
        void registerToken_nullOrBlankToken_doesNothing() {
            // Act
            fcmTokenService.registerToken(TEST_USER_ID, null);
            fcmTokenService.registerToken(TEST_USER_ID, "   ");
            fcmTokenService.registerToken(TEST_USER_ID, "");

            // Assert
            verify(fcmTokenRepository, never()).existsByFcmToken(anyString());
            verify(fcmTokenRepository, never()).save(any(UserFcmToken.class));
        }
    }

    /**
     * Test suite focusing on token removal logic (single device logout).
     */
    @Nested
    @DisplayName("removeToken()")
    class RemoveTokenTests {

        /**
         * Validates that providing a valid token triggers the deletion repository method.
         */
        @Test
        @DisplayName("Valid token → calls deleteByFcmToken")
        void removeToken_validToken_callsDelete() {
            // Act
            fcmTokenService.removeToken(VALID_TOKEN);

            // Assert
            verify(fcmTokenRepository, times(1)).deleteByFcmToken(VALID_TOKEN);
        }

        /**
         * Ensures that passing null or empty values safely aborts the deletion process.
         */
        @Test
        @DisplayName("Null or blank token → ignores deletion")
        void removeToken_nullOrBlankToken_doesNothing() {
            // Act
            fcmTokenService.removeToken(null);
            fcmTokenService.removeToken("   ");

            // Assert
            verify(fcmTokenRepository, never()).deleteByFcmToken(anyString());
        }
    }

    /**
     * Test suite focusing on retrieval of tokens for push notification dispatch.
     */
    @Nested
    @DisplayName("getTokensForUser()")
    class GetTokensForUserTests {

        /**
         * Verifies that the service properly extracts and maps the raw string tokens
         * from the underlying entity objects.
         */
        @Test
        @DisplayName("Valid user with devices → returns mapped string list")
        void getTokensForUser_returnsMappedStringList() {
            // Arrange
            UserFcmToken token1 = new UserFcmToken();
            token1.setFcmToken("token-A");

            UserFcmToken token2 = new UserFcmToken();
            token2.setFcmToken("token-B");

            when(fcmTokenRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(token1, token2));

            // Act
            List<String> results = fcmTokenService.getTokensForUser(TEST_USER_ID);

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.contains("token-A"));
            assertTrue(results.contains("token-B"));

            verify(fcmTokenRepository, times(1)).findByUserId(TEST_USER_ID);
        }
    }

    /**
     * Test suite focusing on bulk token cleanup (account deletion).
     */
    @Nested
    @DisplayName("removeAllTokensForUser()")
    class RemoveAllTokensForUserTests {

        /**
         * Confirms that passing a userId correctly triggers the bulk deletion method.
         */
        @Test
        @DisplayName("Valid userId → calls deleteByUserId")
        void removeAllTokensForUser_validUserId_callsDelete() {
            // Act
            fcmTokenService.removeAllTokensForUser(TEST_USER_ID);

            // Assert
            verify(fcmTokenRepository, times(1)).deleteByUserId(TEST_USER_ID);
        }
    }
}
package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Integer> {

    List<UserFcmToken> findByUserId(Integer userId);

    /** Fetch a specific token (to check if it already exists) */
    boolean existsByFcmToken(String fcmToken);

    /** Remove a specific device token on logout */
    @Modifying
    @Transactional
    void deleteByFcmToken(String fcmToken);

    /** Remove all tokens for a user (e.g., upon account deletion) */
    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);
}
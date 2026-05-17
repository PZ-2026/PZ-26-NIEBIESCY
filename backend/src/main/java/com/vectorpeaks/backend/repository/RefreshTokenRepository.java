package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    /** Invalidate all tokens for a given user (e.g., logout from all devices) */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(Integer userId);
}

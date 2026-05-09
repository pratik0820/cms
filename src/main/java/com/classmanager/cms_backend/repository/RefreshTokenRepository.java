package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUser_IdAndRevokedAtIsNull(UUID userId);

    // Revoke all refresh tokens for a user (used on logout-all-devices)
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now, rt.revokedReason = :reason " +
            "WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    void revokeAllForUser(@Param("userId") UUID userId,
                          @Param("now") LocalDateTime now,
                          @Param("reason") String reason);

    // Cleanup job — delete expired tokens older than X days
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    void deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);

    long countByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(UUID userId, LocalDateTime now);
}

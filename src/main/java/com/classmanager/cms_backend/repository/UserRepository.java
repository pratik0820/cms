package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmailAndIsDeletedFalse(@Param("email") String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID uuid);

    Optional<User> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);

    boolean existsByEmailAndTenantIdAndIsDeletedFalse(String email, UUID tenantId);

    @Modifying
    @Query("UPDATE User u SET u.fcmToken = :fcmToken WHERE u.id = :userId")
    void updateFcmToken(@Param("userId") UUID userId, @Param("fcmToken") String fcmToken);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenantId = :tenantId AND u.isDeleted = false")
    Optional<User> findByEmailAndTenantIdAndIsDeletedFalse(@Param("email") String email, @Param("tenantId") UUID requestedTenantId);

    @Query("""
            SELECT s.user FROM Student s
            WHERE lower(s.loginId) = lower(:loginId)
              AND s.tenantId = :tenantId
              AND s.isDeleted = false
              AND s.user.isDeleted = false
            """)
    Optional<User> findStudentUserByLoginIdAndTenantId(@Param("loginId") String loginId,
                                                       @Param("tenantId") UUID tenantId);
}

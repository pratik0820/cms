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

    @Query("SELECT u FROM User u WHERE lower(u.email) = lower(:email) AND u.isDeleted = false")
    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE lower(u.loginId) = lower(:loginId) AND u.isDeleted = false")
    Optional<User> findByLoginIdIgnoreCaseAndIsDeletedFalse(@Param("loginId") String loginId);

    Optional<User> findByIdAndIsDeletedFalse(UUID uuid);

    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);

    boolean existsByLoginIdIgnoreCaseAndIsDeletedFalse(String loginId);

    @Modifying
    @Query("UPDATE User u SET u.fcmToken = :fcmToken WHERE u.id = :userId")
    void updateFcmToken(@Param("userId") UUID userId, @Param("fcmToken") String fcmToken);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("""
            select count(distinct u)
            from User u
            join u.roles r
            where r.name = :roleName
              and u.isDeleted = false
            """)
    long countByRoleName(@Param("roleName") String roleName);

    @Query("""
            select count(distinct u)
            from User u
            join u.roles r
            where r.name = :roleName
              and u.branch.id = :branchId
              and u.isDeleted = false
            """)
    long countByRoleNameAndBranchId(@Param("roleName") String roleName, @Param("branchId") UUID branchId);

    @Query("""
            select count(distinct u)
            from User u
            join u.roles r
            where r.name = :roleName
              and u.isDeleted = false
              and u.createdAt >= :from
              and u.createdAt < :to
            """)
    long countByRoleNameCreatedBetween(@Param("roleName") String roleName,
                                       @Param("from") java.time.LocalDateTime from,
                                       @Param("to") java.time.LocalDateTime to);

    @Query("""
            select count(distinct u)
            from User u
            join u.roles r
            where r.name = :roleName
              and u.branch.id = :branchId
              and u.isDeleted = false
              and u.createdAt >= :from
              and u.createdAt < :to
            """)
    long countByRoleNameAndBranchIdCreatedBetween(@Param("roleName") String roleName,
                                                  @Param("branchId") UUID branchId,
                                                  @Param("from") java.time.LocalDateTime from,
                                                  @Param("to") java.time.LocalDateTime to);

    @Query("""
            select case when count(u) > 0 then true else false end
            from User u
            join u.roles r
            where r.name = :roleName
              and u.isDeleted = false
            """)
    boolean existsByRoleName(@Param("roleName") String roleName);

    @Query("""
            select count(distinct u)
            from User u
            where u.branch.id = :branchId
              and u.isDeleted = false
            """)
    long countByBranchId(@Param("branchId") UUID branchId);
}

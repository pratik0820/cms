package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.AdminProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, UUID> {

    @Query("""
            select ap
            from AdminProfile ap
            join ap.user u
            where ap.id = :adminProfileId
              and ap.isDeleted = false
              and u.isDeleted = false
            """)
    Optional<AdminProfile> findActiveById(@Param("adminProfileId") UUID adminProfileId);

    @Query("""
            select ap
            from AdminProfile ap
            join ap.user u
            where u.id = :userId
              and ap.isDeleted = false
              and u.isDeleted = false
            """)
    Optional<AdminProfile> findByUserIdAndIsDeletedFalse(@Param("userId") UUID userId);

    @Query("""
            select ap
            from AdminProfile ap
            join ap.user u
            left join u.branch b
            where ap.isDeleted = false
              and u.isDeleted = false
              and (
                    :searchPattern is null
                    or lower(u.fullName) like :searchPattern
                    or lower(u.email) like :searchPattern
                    or lower(coalesce(u.phone, '')) like :searchPattern
                    or lower(coalesce(u.loginId, '')) like :searchPattern
              )
              and (:isActive is null or u.isActive = :isActive)
              and (
                    :branchId is null
                    or ap.allBranchesAccess = true
                    or b.id = :branchId
              )
            """)
    Page<AdminProfile> searchAdminProfiles(@Param("searchPattern") String searchPattern,
                                           @Param("isActive") Boolean isActive,
                                           @Param("branchId") UUID branchId,
                                           Pageable pageable);

    @Query("""
            select count(ap)
            from AdminProfile ap
            join ap.user u
            left join u.branch b
            where ap.isDeleted = false
              and u.isDeleted = false
              and (
                    :branchId is null
                    or ap.allBranchesAccess = true
                    or b.id = :branchId
              )
            """)
    long countTotalAdmins(@Param("branchId") UUID branchId);

    @Query("""
            select count(ap)
            from AdminProfile ap
            join ap.user u
            left join u.branch b
            where ap.isDeleted = false
              and u.isDeleted = false
              and u.isActive = true
              and (
                    :branchId is null
                    or ap.allBranchesAccess = true
                    or b.id = :branchId
              )
            """)
    long countActiveAdmins(@Param("branchId") UUID branchId);

    @Query("""
            select count(ap)
            from AdminProfile ap
            join ap.user u
            left join u.branch b
            where ap.isDeleted = false
              and u.isDeleted = false
              and u.isActive = false
              and (
                    :branchId is null
                    or ap.allBranchesAccess = true
                    or b.id = :branchId
              )
            """)
    long countInactiveAdmins(@Param("branchId") UUID branchId);

    @Query("""
            select count(ap)
            from AdminProfile ap
            join ap.user u
            where ap.isDeleted = false
              and u.isDeleted = false
              and ap.allBranchesAccess = true
            """)
    long countAllBranchAdmins();
}

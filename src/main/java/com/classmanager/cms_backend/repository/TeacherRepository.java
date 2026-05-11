package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByIdAndIsDeletedFalse(UUID id);

    Optional<Teacher> findByUser_IdAndIsDeletedFalse(UUID userId);

    @Query("""
            select t
            from Teacher t
            join t.user u
            join t.branch b
            where t.id = :teacherId
              and t.isDeleted = false
              and u.isDeleted = false
            """)
    Optional<Teacher> findActiveById(@Param("teacherId") UUID teacherId);

    @Query("""
            select distinct t
            from Teacher t
            join t.user u
            join t.branch b
            left join t.subjects s
            where t.isDeleted = false
              and u.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and (:isActive is null or t.isActive = :isActive)
              and (:subject is null or lower(s) = lower(:subject))
              and (
                    :searchPattern is null
                    or lower(t.name) like :searchPattern
                    or lower(t.email) like :searchPattern
                    or lower(coalesce(t.phone, '')) like :searchPattern
                    or lower(coalesce(u.loginId, '')) like :searchPattern
              )
            """)
    Page<Teacher> searchTeachers(@Param("searchPattern") String searchPattern,
                                 @Param("isActive") Boolean isActive,
                                 @Param("branchId") UUID branchId,
                                 @Param("subject") String subject,
                                 Pageable pageable);

    @Query("""
            select count(distinct t)
            from Teacher t
            join t.user u
            join t.branch b
            where t.isDeleted = false
              and u.isDeleted = false
              and (:branchId is null or b.id = :branchId)
            """)
    long countTotalTeachers(@Param("branchId") UUID branchId);

    @Query("""
            select count(distinct t)
            from Teacher t
            join t.user u
            join t.branch b
            where t.isDeleted = false
              and u.isDeleted = false
              and t.isActive = true
              and (:branchId is null or b.id = :branchId)
            """)
    long countActiveTeachers(@Param("branchId") UUID branchId);

    @Query("""
            select count(distinct t)
            from Teacher t
            join t.user u
            join t.branch b
            where t.isDeleted = false
              and u.isDeleted = false
              and t.isActive = false
              and (:branchId is null or b.id = :branchId)
            """)
    long countInactiveTeachers(@Param("branchId") UUID branchId);

    Page<Teacher> findByBranch_IdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Teacher> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    long countByIsDeletedFalse();

    long countByBranch_IdAndIsDeletedFalse(UUID branchId);

    long countByIsActiveTrueAndIsDeletedFalse();

    long countByBranch_IdAndIsActiveTrueAndIsDeletedFalse(UUID branchId);

    long countByCreatedAtBetweenAndIsDeletedFalse(java.time.LocalDateTime from, java.time.LocalDateTime to);

    long countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(UUID branchId,
                                                              java.time.LocalDateTime from,
                                                              java.time.LocalDateTime to);
}

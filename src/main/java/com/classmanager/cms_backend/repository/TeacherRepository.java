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
            left join t.subjects legacySubject
            left join t.catalogSubjects catalogSubject
            left join t.courses course
            left join t.batches batch
            where t.isDeleted = false
              and u.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and (:isActive is null or t.isActive = :isActive)
              and (
                    cast(:subject as string) is null
                    or lower(legacySubject) = lower(cast(:subject as string))
                    or lower(catalogSubject.displayName) = lower(cast(:subject as string))
                    or lower(cast(catalogSubject.code as string)) = lower(cast(:subject as string))
              )
              and (:subjectId is null or catalogSubject.id = :subjectId)
              and (:courseId is null or course.id = :courseId)
              and (:batchId is null or batch.id = :batchId)
              and (
                    cast(:searchPattern as string) is null
                    or lower(t.name) like cast(:searchPattern as string)
                    or lower(t.email) like cast(:searchPattern as string)
                    or lower(coalesce(t.phone, '')) like cast(:searchPattern as string)
                    or lower(coalesce(cast(u.loginId as string), '')) like cast(:searchPattern as string)
              )
            """)
    Page<Teacher> searchTeachers(@Param("searchPattern") String searchPattern,
                                 @Param("isActive") Boolean isActive,
                                 @Param("branchId") UUID branchId,
                                 @Param("subject") String subject,
                                 @Param("subjectId") UUID subjectId,
                                 @Param("courseId") UUID courseId,
                                 @Param("batchId") UUID batchId,
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

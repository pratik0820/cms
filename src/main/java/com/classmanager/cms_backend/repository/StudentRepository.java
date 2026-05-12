package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByIdAndIsDeletedFalse(UUID id);

    Optional<Student> findByUser_IdAndIsDeletedFalse(UUID userId);

    Optional<Student> findByStudentIdIgnoreCaseAndIsDeletedFalse(String studentId);

    boolean existsByStudentIdIgnoreCaseAndIsDeletedFalse(String studentId);

    @Query("""
            select s
            from Student s
            left join s.user u
            where s.id = :studentId
              and s.isDeleted = false
              and (u is null or u.isDeleted = false)
            """)
    Optional<Student> findActiveById(@Param("studentId") UUID studentId);

    @Query("""
            select s
                       from Student s
                       left join s.user u
                       join s.branch b
                       where s.isDeleted = false
                         and (u is null or u.isDeleted = false)
                         and (cast(:branchId as java.util.UUID) is null or b.id = :branchId)
                         and (:isActive is null or s.isActive = :isActive)
                         and (cast(:standard as string) is null or lower(s.standard) = lower(cast(:standard as string)))
                         and (cast(:batch as string) is null or lower(s.batch) = lower(cast(:batch as string)))
                         and (
                               cast(:searchPattern as string) is null
                               or lower(s.name) like cast(:searchPattern as string)
                               or lower(coalesce(s.studentId, '')) like cast(:searchPattern as string)
                               or lower(coalesce(s.mobile, '')) like cast(:searchPattern as string)
                               or lower(coalesce(s.email, '')) like cast(:searchPattern as string)
                               or lower(coalesce(s.parentName, '')) like cast(:searchPattern as string)
                               or lower(coalesce(s.parentPhone, '')) like cast(:searchPattern as string)
                               or lower(coalesce(u.loginId, '')) like cast(:searchPattern as string)
                         )
            """)
    Page<Student> searchStudents(@Param("searchPattern") String searchPattern,
                                 @Param("isActive") Boolean isActive,
                                 @Param("branchId") UUID branchId,
                                 @Param("standard") String standard,
                                 @Param("batch") String batch,
                                 Pageable pageable);

    @Query("""
            select count(s)
            from Student s
            left join s.user u
            join s.branch b
            where s.isDeleted = false
              and (u is null or u.isDeleted = false)
              and (:branchId is null or b.id = :branchId)
            """)
    long countTotalStudents(@Param("branchId") UUID branchId);

    @Query("""
            select count(s)
            from Student s
            left join s.user u
            join s.branch b
            where s.isDeleted = false
              and (u is null or u.isDeleted = false)
              and s.isActive = true
              and (:branchId is null or b.id = :branchId)
            """)
    long countActiveStudents(@Param("branchId") UUID branchId);

    @Query("""
            select count(s)
            from Student s
            left join s.user u
            join s.branch b
            where s.isDeleted = false
              and (u is null or u.isDeleted = false)
              and s.isActive = false
              and (:branchId is null or b.id = :branchId)
            """)
    long countInactiveStudents(@Param("branchId") UUID branchId);

    Page<Student> findByBranch_IdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Student> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    long countByIsDeletedFalse();

    long countByBranch_IdAndIsDeletedFalse(UUID branchId);

    long countByIsActiveTrueAndIsDeletedFalse();

    long countByCreatedAtBetweenAndIsDeletedFalse(java.time.LocalDateTime from, java.time.LocalDateTime to);

    long countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(UUID branchId,
                                                              java.time.LocalDateTime from,
                                                              java.time.LocalDateTime to);

    long countByCreatedAtBeforeAndIsDeletedFalse(java.time.LocalDateTime before);

    long countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(UUID branchId, java.time.LocalDateTime before);

    @Query("""
            select s
            from Student s
            left join s.user u
            join fetch s.branch b
            where s.isDeleted = false
              and (u is null or u.isDeleted = false)
              and (:branchId is null or b.id = :branchId)
            order by s.createdAt asc
            """)
    List<Student> findAnalyticsStudents(@Param("branchId") UUID branchId);

    @Query("""
            select count(s)
            from Student s
            left join s.user u
            join s.branch b
            where s.isDeleted = false
              and (u is null or u.isDeleted = false)
              and s.isAdmissionFinal = true
              and (:branchId is null or b.id = :branchId)
              and s.admissionDate >= :fromDate
              and s.admissionDate <= :toDate
            """)
    long countAdmissionsBetween(@Param("branchId") UUID branchId,
                                @Param("fromDate") java.time.LocalDate fromDate,
                                @Param("toDate") java.time.LocalDate toDate);

    @Query("SELECT s FROM Student s WHERE s.isAdmissionFinal = false AND s.isDeleted = false")
    Page<Student> findDraftAdmissions(Pageable pageable);
}

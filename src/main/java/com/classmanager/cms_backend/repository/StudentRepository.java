package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
              and (:branchId is null or b.id = :branchId)
              and (:isActive is null or s.isActive = :isActive)
              and (:standard is null or lower(s.standard) = lower(:standard))
              and (:batch is null or lower(s.batch) = lower(:batch))
              and (
                    :searchPattern is null
                    or lower(s.name) like :searchPattern
                    or lower(coalesce(s.studentId, '')) like :searchPattern
                    or lower(coalesce(s.mobile, '')) like :searchPattern
                    or lower(coalesce(s.email, '')) like :searchPattern
                    or lower(coalesce(s.parentName, '')) like :searchPattern
                    or lower(coalesce(s.parentPhone, '')) like :searchPattern
                    or lower(coalesce(u.loginId, '')) like :searchPattern
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

    @Query("SELECT s FROM Student s WHERE s.isAdmissionFinal = false AND s.isDeleted = false")
    Page<Student> findDraftAdmissions(Pageable pageable);
}

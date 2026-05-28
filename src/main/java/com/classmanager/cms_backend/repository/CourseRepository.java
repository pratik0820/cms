package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.enums.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc();

    List<Course> findByStandardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(String standard);

    List<Course> findByBoardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(BoardType board);

    List<Course> findByBoardAndStandardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(BoardType board, String standard);

    List<Course> findByCategoryAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(CourseCategory category);

    List<Course> findByCategoryAndStandardAndIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc(CourseCategory category, String standard);

    Optional<Course> findByCodeAndIsDeletedFalse(String code);

    Optional<Course> findByIdAndIsDeletedFalse(UUID id);

    Optional<Course> findByIdAndIsActiveTrueAndIsDeletedFalse(UUID id);

    boolean existsByCodeAndIsDeletedFalse(String code);

    /**
     * Fetch course with subject groups eagerly for enrolment flows that still
     * use subject-group based selection.
     */
    @Query("""
            SELECT DISTINCT c FROM Course c
            LEFT JOIN FETCH c.subjectGroups sg
            WHERE c.id = :id AND c.isDeleted = false
            """)
    Optional<Course> findByIdWithSubjectGroups(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT c FROM Course c
            LEFT JOIN FETCH c.standardRef s
            LEFT JOIN FETCH c.subjects subjects
            WHERE c.id = :id AND c.isDeleted = false
            """)
    Optional<Course> findAcademicCourseDetail(@Param("id") UUID id);
}

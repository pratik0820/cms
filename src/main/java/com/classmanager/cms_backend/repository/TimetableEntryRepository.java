package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, UUID> {
    List<TimetableEntry> findByTimetableIdAndIsDeletedFalse(UUID timetableId);

    @Query("SELECT e FROM TimetableEntry e WHERE e.isDeleted = false AND e.classDate BETWEEN :startDate AND :endDate")
    List<TimetableEntry> findByClassDateBetweenAndIsDeletedFalse(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM TimetableEntry e JOIN e.timetable t WHERE e.isDeleted = false AND t.teacher.id = :teacherId AND e.classDate BETWEEN :startDate AND :endDate")
    List<TimetableEntry> findByTeacherIdAndClassDateBetweenAndIsDeletedFalse(@Param("teacherId") UUID teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM TimetableEntry e JOIN e.timetable t WHERE e.isDeleted = false AND t.teacher.id = :teacherId AND e.classDate = :classDate")
    List<TimetableEntry> findByTeacherIdAndClassDateAndIsDeletedFalse(@Param("teacherId") UUID teacherId, @Param("classDate") LocalDate classDate);

    @Query("SELECT e FROM TimetableEntry e WHERE e.isDeleted = false AND e.batch.id IN :batchIds AND e.classDate BETWEEN :startDate AND :endDate")
    List<TimetableEntry> findByBatchIdInAndClassDateBetweenAndIsDeletedFalse(@Param("batchIds") List<UUID> batchIds, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

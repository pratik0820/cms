package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, UUID> {
    Optional<Timetable> findByIdAndIsDeletedFalse(UUID id);
    List<Timetable> findByTeacherIdAndIsDeletedFalseOrderByEffectiveDateDesc(UUID teacherId);
    Optional<Timetable> findByTeacherIdAndEffectiveDateAndIsDeletedFalse(UUID teacherId, LocalDate effectiveDate);
    List<Timetable> findAllByIsDeletedFalseOrderByEffectiveDateDesc();
}

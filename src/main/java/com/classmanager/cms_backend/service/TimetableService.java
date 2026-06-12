package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.SaveTimetableRequest;
import com.classmanager.cms_backend.dto.request.TimetableEntryRequest;
import com.classmanager.cms_backend.dto.response.TimetableEntryResponse;
import com.classmanager.cms_backend.dto.response.TimetableResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.entity.Teacher;
import com.classmanager.cms_backend.entity.Timetable;
import com.classmanager.cms_backend.entity.TimetableEntry;
import com.classmanager.cms_backend.entity.Student;
import com.classmanager.cms_backend.entity.StudentEnrolment;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BatchRepository;
import com.classmanager.cms_backend.repository.StudentEnrolmentRepository;
import com.classmanager.cms_backend.repository.StudentRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import com.classmanager.cms_backend.repository.TeacherRepository;
import com.classmanager.cms_backend.repository.TimetableEntryRepository;
import com.classmanager.cms_backend.repository.TimetableRepository;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final BatchRepository batchRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrolmentRepository studentEnrolmentRepository;

    @Transactional(readOnly = true)
    public TimetableResponse getTimetable(UUID teacherId, LocalDate effectiveDate) {
        Optional<Timetable> timetableOpt;
        if (effectiveDate != null) {
            timetableOpt = timetableRepository.findByTeacherIdAndEffectiveDateAndIsDeletedFalse(teacherId, effectiveDate);
        } else {
            timetableOpt = timetableRepository.findByTeacherIdAndIsDeletedFalseOrderByEffectiveDateDesc(teacherId).stream().findFirst();
        }

        if (timetableOpt.isEmpty()) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
            return TimetableResponse.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacher.getName())
                    .effectiveDate(effectiveDate != null ? effectiveDate : LocalDate.now())
                    .viewType("Weekly")
                    .entries(new ArrayList<>())
                    .build();
        }

        return toResponse(timetableOpt.get());
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> getAllTimetables(LocalDate effectiveDate) {
        List<Timetable> timetables = timetableRepository.findAllByIsDeletedFalseOrderByEffectiveDateDesc();
        if (effectiveDate != null) {
            timetables = timetables.stream()
                .filter(t -> t.getEffectiveDate().equals(effectiveDate))
                .collect(Collectors.toList());
        }
        return timetables.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimetableResponse saveTimetable(SaveTimetableRequest request) {
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", request.getTeacherId()));

        Optional<Timetable> existingOpt = timetableRepository.findByTeacherIdAndEffectiveDateAndIsDeletedFalse(
                request.getTeacherId(), request.getEffectiveDate());

        Timetable timetable;
        if (existingOpt.isPresent()) {
            timetable = existingOpt.get();
            timetable.getEntries().clear(); // Orphan removal will delete existing entries
            timetableEntryRepository.deleteAll(timetableEntryRepository.findByTimetableIdAndIsDeletedFalse(timetable.getId()));
        } else {
            timetable = Timetable.builder()
                    .teacher(teacher)
                    .branch(teacher.getBranch())
                    .effectiveDate(request.getEffectiveDate())
                    .viewType(request.getViewType() != null ? request.getViewType() : "Weekly")
                    .isActive(true)
                    .build();
        }

        timetableRepository.save(timetable);

        if (request.getEntries() != null) {
            for (TimetableEntryRequest entryReq : request.getEntries()) {
                Subject subject = entryReq.getSubjectId() != null ? 
                        subjectRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(entryReq.getSubjectId()).orElse(null) : null;
                Batch batch = entryReq.getBatchId() != null ? 
                        batchRepository.findByIdAndIsDeletedFalse(entryReq.getBatchId()).orElse(null) : null;

                TimetableEntry entry = TimetableEntry.builder()
                        .timetable(timetable)
                        .dayOfWeek(entryReq.getDayOfWeek().toUpperCase())
                        .startTime(entryReq.getStartTime())
                        .endTime(entryReq.getEndTime())
                        .subject(subject)
                        .batch(batch)
                        .room(entryReq.getRoom())
                        .periodType(entryReq.getPeriodType() != null ? entryReq.getPeriodType() : "CLASS")
                        .isActive(true)
                        .build();

                timetable.addEntry(entry);
                timetableEntryRepository.save(entry);
            }
        }

        return toResponse(timetable);
    }

    @Transactional
    public void deleteTimetable(UUID timetableId) {
        Timetable timetable = timetableRepository.findByIdAndIsDeletedFalse(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", timetableId));
        
        timetable.softDelete();
        timetable.setIsActive(false);
        timetableRepository.save(timetable);
        
        List<TimetableEntry> entries = timetableEntryRepository.findByTimetableIdAndIsDeletedFalse(timetableId);
        entries.forEach(entry -> {
            entry.softDelete();
            entry.setIsActive(false);
            timetableEntryRepository.save(entry);
        });
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> getCalendarTimetables(LocalDate startDate, LocalDate endDate, UUID teacherId) {
        List<TimetableEntry> entries;
        if (teacherId != null) {
            entries = timetableEntryRepository.findByTeacherIdAndClassDateBetweenAndIsDeletedFalse(teacherId, startDate, endDate);
        } else {
            entries = timetableEntryRepository.findByClassDateBetweenAndIsDeletedFalse(startDate, endDate);
        }
        return entries.stream().map(this::toEntryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> getMyCalendarTimetables(LocalDate startDate, LocalDate endDate, CmsUserDetails currentUser) {
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
            Teacher teacher = teacherRepository.findByUser_IdAndIsDeletedFalse(currentUser.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for user", currentUser.getUserId()));
            return timetableEntryRepository.findByTeacherIdAndClassDateBetweenAndIsDeletedFalse(teacher.getId(), startDate, endDate)
                    .stream().map(this::toEntryResponse).collect(Collectors.toList());
        } else if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            Student student = studentRepository.findByUser_IdAndIsDeletedFalse(currentUser.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user", currentUser.getUserId()));
            List<UUID> enrolledBatchIds = studentEnrolmentRepository.findByStudent_IdAndIsDeletedFalseOrderByEnrolmentDateDesc(student.getId())
                    .stream()
                    .filter(e -> "ACTIVE".equals(e.getStatus().name()))
                    .map(e -> e.getBatch().getId())
                    .collect(Collectors.toList());
            if (enrolledBatchIds.isEmpty()) {
                return new ArrayList<>();
            }
            return timetableEntryRepository.findByBatchIdInAndClassDateBetweenAndIsDeletedFalse(enrolledBatchIds, startDate, endDate)
                    .stream().map(this::toEntryResponse).collect(Collectors.toList());
        }
        throw new com.classmanager.cms_backend.exception.BadRequestException("User is not authorized to view personal timetables", "INVALID_ROLE");
    }

    @Transactional
    public TimetableEntryResponse scheduleClass(UUID teacherId, TimetableEntryRequest entryReq) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        // Find a general timetable for the teacher or create one
        Timetable timetable = timetableRepository.findByTeacherIdAndIsDeletedFalseOrderByEffectiveDateDesc(teacherId)
                .stream().findFirst().orElseGet(() -> {
                    Timetable newTimetable = Timetable.builder()
                            .teacher(teacher)
                            .branch(teacher.getBranch())
                            .effectiveDate(LocalDate.now()) // Just a default
                            .viewType("General")
                            .isActive(true)
                            .build();
                    return timetableRepository.save(newTimetable);
                });

        Subject subject = entryReq.getSubjectId() != null ? 
                subjectRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(entryReq.getSubjectId()).orElse(null) : null;
        Batch batch = entryReq.getBatchId() != null ? 
                batchRepository.findByIdAndIsDeletedFalse(entryReq.getBatchId()).orElse(null) : null;

        TimetableEntry entry = TimetableEntry.builder()
                .timetable(timetable)
                .classDate(entryReq.getClassDate())
                .dayOfWeek(entryReq.getDayOfWeek() != null ? entryReq.getDayOfWeek().toUpperCase() : entryReq.getClassDate().getDayOfWeek().name())
                .startTime(entryReq.getStartTime())
                .endTime(entryReq.getEndTime())
                .subject(subject)
                .batch(batch)
                .room(entryReq.getRoom())
                .periodType(entryReq.getPeriodType() != null ? entryReq.getPeriodType() : "CLASS")
                .isActive(true)
                .build();

        timetable.addEntry(entry);
        timetableEntryRepository.save(entry);

        return toEntryResponse(entry);
    }

    @Transactional
    public TimetableEntryResponse updateScheduledClass(UUID entryId, TimetableEntryRequest entryReq) {
        TimetableEntry entry = timetableEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableEntry", entryId));

        Subject subject = entryReq.getSubjectId() != null ? 
                subjectRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(entryReq.getSubjectId()).orElse(null) : null;
        Batch batch = entryReq.getBatchId() != null ? 
                batchRepository.findByIdAndIsDeletedFalse(entryReq.getBatchId()).orElse(null) : null;

        entry.setClassDate(entryReq.getClassDate());
        entry.setDayOfWeek(entryReq.getDayOfWeek() != null ? entryReq.getDayOfWeek().toUpperCase() : entryReq.getClassDate().getDayOfWeek().name());
        entry.setStartTime(entryReq.getStartTime());
        entry.setEndTime(entryReq.getEndTime());
        entry.setSubject(subject);
        entry.setBatch(batch);
        entry.setRoom(entryReq.getRoom());
        if (entryReq.getPeriodType() != null) {
            entry.setPeriodType(entryReq.getPeriodType());
        }

        timetableEntryRepository.save(entry);
        return toEntryResponse(entry);
    }

    @Transactional
    public void deleteScheduledClass(UUID entryId) {
        TimetableEntry entry = timetableEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableEntry", entryId));
        entry.softDelete();
        entry.setIsActive(false);
        timetableEntryRepository.save(entry);
    }

    private TimetableResponse toResponse(Timetable timetable) {
        return TimetableResponse.builder()
                .id(timetable.getId())
                .teacherId(timetable.getTeacher().getId())
                .teacherName(timetable.getTeacher().getName())
                .effectiveDate(timetable.getEffectiveDate())
                .viewType(timetable.getViewType())
                .entries(timetable.getEntries().stream()
                        .filter(e -> !e.isDeleted())
                        .map(this::toEntryResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private TimetableEntryResponse toEntryResponse(TimetableEntry entry) {
        return TimetableEntryResponse.builder()
                .id(entry.getId())
                .classDate(entry.getClassDate())
                .dayOfWeek(entry.getDayOfWeek())
                .teacherId(entry.getTimetable() != null && entry.getTimetable().getTeacher() != null ? entry.getTimetable().getTeacher().getId() : null)
                .teacherName(entry.getTimetable() != null && entry.getTimetable().getTeacher() != null ? entry.getTimetable().getTeacher().getName() : null)
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .subjectId(entry.getSubject() != null ? entry.getSubject().getId() : null)
                .subjectName(entry.getSubject() != null ? entry.getSubject().getDisplayName() : null)
                .batchId(entry.getBatch() != null ? entry.getBatch().getId() : null)
                .batchName(entry.getBatch() != null ? entry.getBatch().getName() : null)
                .section(entry.getBatch() != null ? entry.getBatch().getSection() : null)
                .room(entry.getRoom())
                .periodType(entry.getPeriodType())
                .build();
    }
}

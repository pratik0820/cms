package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.UpdateProgressRequest;
import com.classmanager.cms_backend.dto.response.AcademicCourseListItemResponse;
import com.classmanager.cms_backend.dto.response.BatchSubjectSyllabusResponse;
import com.classmanager.cms_backend.dto.response.ChapterProgressResponse;
import com.classmanager.cms_backend.dto.response.SubtopicProgressResponse;
import com.classmanager.cms_backend.entity.*;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyllabusProgressService {

    private final TeacherRepository teacherRepository;
    private final ChapterRepository chapterRepository;
    private final BatchSubtopicProgressRepository progressRepository;
    private final BatchRepository batchRepository;
    private final SubtopicRepository subtopicRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AcademicCourseListItemResponse> getMyAssignments(UUID userId) {
        Teacher teacher = teacherRepository.findByUser_IdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for user", userId));

        return teacher.getBatches().stream()
                .filter(b -> !b.isDeleted())
                .map(this::toAcademicCourseListItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BatchSubjectSyllabusResponse> getAllMyProgress(UUID userId) {
        Teacher teacher = teacherRepository.findByUser_IdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for user", userId));

        List<BatchSubjectSyllabusResponse> result = new java.util.ArrayList<>();

        for (Batch batch : teacher.getBatches()) {
            if (batch.isDeleted()) continue;
            Course course = batch.getCourse();
            if (course == null || course.isDeleted()) continue;

            // Fetch all progress for this batch once
            List<BatchSubtopicProgress> batchProgressList = progressRepository.findByBatchId(batch.getId());
            Map<UUID, BatchSubtopicProgress> progressMap = batchProgressList.stream()
                    .collect(Collectors.toMap(p -> p.getSubtopic().getId(), p -> p));

            for (Subject subject : course.getSubjects()) {
                if (subject.isDeleted() || !subject.getIsActive()) continue;

                List<Chapter> chapters = chapterRepository.findByCourseIdAndSubjectIdAndIsDeletedFalseOrderByCreatedAtAsc(course.getId(), subject.getId());
                
                List<ChapterProgressResponse> chapterResponses = chapters.stream().map(chapter -> ChapterProgressResponse.builder()
                        .id(chapter.getId())
                        .name(chapter.getName())
                        .targetDate(chapter.getTargetDate())
                        .subtopics(chapter.getSubtopics().stream()
                                .filter(st -> !st.getIsDeleted())
                                .map(st -> {
                                    BatchSubtopicProgress p = progressMap.get(st.getId());
                                    return SubtopicProgressResponse.builder()
                                            .id(st.getId())
                                            .name(st.getName())
                                            .totalQuestions(st.getNoOfQuestions())
                                            .completedQuestions(p != null ? p.getCompletedQuestions() : 0)
                                            .status(p != null ? p.getStatus() : "PENDING")
                                            .build();
                                }).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());

                result.add(BatchSubjectSyllabusResponse.builder()
                        .batchId(batch.getId())
                        .batchName(batch.getName())
                        .courseName(course.getName())
                        .subjectId(subject.getId())
                        .subjectName(subject.getDisplayName())
                        .chapters(chapterResponses)
                        .build());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ChapterProgressResponse> getBatchProgress(UUID batchId, UUID subjectId) {
        Batch batch = batchRepository.findByIdAndIsDeletedFalse(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", batchId));

        List<Chapter> chapters = chapterRepository.findByCourseIdAndSubjectIdAndIsDeletedFalseOrderByCreatedAtAsc(batch.getCourse().getId(), subjectId);
        List<BatchSubtopicProgress> progressList = progressRepository.findByBatchId(batchId);

        Map<UUID, BatchSubtopicProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getSubtopic().getId(), p -> p));

        return chapters.stream().map(chapter -> ChapterProgressResponse.builder()
                .id(chapter.getId())
                .name(chapter.getName())
                .targetDate(chapter.getTargetDate())
                .subtopics(chapter.getSubtopics().stream()
                        .filter(st -> !st.getIsDeleted())
                        .map(st -> {
                            BatchSubtopicProgress p = progressMap.get(st.getId());
                            return SubtopicProgressResponse.builder()
                                    .id(st.getId())
                                    .name(st.getName())
                                    .totalQuestions(st.getNoOfQuestions())
                                    .completedQuestions(p != null ? p.getCompletedQuestions() : 0)
                                    .status(p != null ? p.getStatus() : "PENDING")
                                    .build();
                        }).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public void updateProgress(UpdateProgressRequest request, UUID userId) {
        Batch batch = batchRepository.findByIdAndIsDeletedFalse(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch", request.getBatchId()));

        Subtopic subtopic = subtopicRepository.findById(request.getSubtopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic", request.getSubtopicId()));

        BatchSubtopicProgress progress = progressRepository.findByBatchIdAndSubtopicId(batch.getId(), subtopic.getId())
                .orElseGet(() -> {
                    User creator = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
                    return BatchSubtopicProgress.builder()
                            .batch(batch)
                            .subtopic(subtopic)
                            .createdByUser(creator)
                            .build();
                });

        if (request.getCompletedQuestions() != null) {
            progress.setCompletedQuestions(request.getCompletedQuestions());
        }
        if (StringUtils.hasText(request.getStatus())) {
            progress.setStatus(request.getStatus());
        }

        progressRepository.save(progress);
    }

    private AcademicCourseListItemResponse toAcademicCourseListItemResponse(Batch batch) {
        Course course = batch.getCourse();
        return AcademicCourseListItemResponse.builder()
                .id(batch.getId())
                .courseUuid(course.getId())
                .courseId(StringUtils.hasText(batch.getDisplayCode()) ? batch.getDisplayCode() : "CRS-" + batch.getId())
                .standard(course.getStandard())
                .board(course.getBoard() != null ? course.getBoard().getDisplayName() : null)
                .medium(course.getMedium())
                .academicYear(course.getAcademicYear())
                .courseName(course.getName())
                .batchName(batch.getName())
                .section(batch.getSection())
                .build();
    }
}

package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.ChapterRequest;
import com.classmanager.cms_backend.dto.request.SubtopicRequest;
import com.classmanager.cms_backend.dto.response.ChapterResponse;
import com.classmanager.cms_backend.dto.response.SubtopicResponse;
import com.classmanager.cms_backend.entity.Chapter;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.entity.Subtopic;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.ChapterRepository;
import com.classmanager.cms_backend.repository.CourseRepository;
import com.classmanager.cms_backend.repository.SubjectRepository;
import com.classmanager.cms_backend.repository.SubtopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final ChapterRepository chapterRepository;
    private final SubtopicRepository subtopicRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<ChapterResponse> getChapters(UUID courseId, UUID subjectId) {
        return chapterRepository.findByCourseIdAndSubjectIdAndIsDeletedFalseOrderByCreatedAtAsc(courseId, subjectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ChapterResponse saveChapter(ChapterRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.getSubjectId()));

        Chapter chapter = Chapter.builder()
                .course(course)
                .subject(subject)
                .name(request.getName())
                .targetDate(request.getTargetDate())
                .subtopics(new ArrayList<>())
                .build();

        if (request.getSubtopics() != null) {
            for (SubtopicRequest stReq : request.getSubtopics()) {
                chapter.getSubtopics().add(Subtopic.builder()
                        .chapter(chapter)
                        .name(stReq.getName())
                        .noOfQuestions(stReq.getNoOfQuestions())
                        .build());
            }
        }

        return toResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public ChapterResponse updateChapter(UUID chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", chapterId));

        chapter.setName(request.getName());
        chapter.setTargetDate(request.getTargetDate());

        if (request.getSubtopics() != null) {
            // Map existing subtopics by ID
            Map<UUID, Subtopic> existingSubtopics = chapter.getSubtopics().stream()
                    .filter(st -> !st.getIsDeleted())
                    .collect(Collectors.toMap(Subtopic::getId, st -> st));

            List<Subtopic> newSubtopicsList = new ArrayList<>();

            for (SubtopicRequest stReq : request.getSubtopics()) {
                if (stReq.getId() != null && existingSubtopics.containsKey(stReq.getId())) {
                    Subtopic existing = existingSubtopics.get(stReq.getId());
                    existing.setName(stReq.getName());
                    existing.setNoOfQuestions(stReq.getNoOfQuestions());
                    newSubtopicsList.add(existing);
                    existingSubtopics.remove(stReq.getId());
                } else {
                    newSubtopicsList.add(Subtopic.builder()
                            .chapter(chapter)
                            .name(stReq.getName())
                            .noOfQuestions(stReq.getNoOfQuestions())
                            .build());
                }
            }

            // Mark remaining as deleted
            existingSubtopics.values().forEach(st -> st.setIsDeleted(true));

            chapter.getSubtopics().clear();
            chapter.getSubtopics().addAll(newSubtopicsList);
            chapter.getSubtopics().addAll(existingSubtopics.values());
        }

        return toResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public void deleteChapter(UUID chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", chapterId));
        chapter.setIsDeleted(true);
        chapter.getSubtopics().forEach(st -> st.setIsDeleted(true));
        chapterRepository.save(chapter);
    }

    private ChapterResponse toResponse(Chapter chapter) {
        return ChapterResponse.builder()
                .id(chapter.getId())
                .courseId(chapter.getCourse().getId())
                .courseName(chapter.getCourse().getName())
                .subjectId(chapter.getSubject().getId())
                .subjectName(chapter.getSubject().getDisplayName())
                .name(chapter.getName())
                .targetDate(chapter.getTargetDate())
                .createdAt(chapter.getCreatedAt())
                .subtopics(chapter.getSubtopics().stream()
                        .filter(st -> !st.getIsDeleted())
                        .map(st -> SubtopicResponse.builder()
                                .id(st.getId())
                                .name(st.getName())
                                .noOfQuestions(st.getNoOfQuestions())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

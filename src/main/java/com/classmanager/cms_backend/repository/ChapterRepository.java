package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    List<Chapter> findByCourseIdAndSubjectIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID courseId, UUID subjectId);
}

package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.entity.Course;
import com.classmanager.cms_backend.entity.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CourseManagementRepository {

    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SubjectRepository subjectRepository;
    private final BranchRepository branchRepository;

    public Page<Batch> searchCourseRows(UUID branchId, String searchPattern, Pageable pageable) {
        return batchRepository.searchAcademicCourses(branchId, searchPattern, pageable);
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public Batch saveBatch(Batch batch) {
        return batchRepository.save(batch);
    }

    public Subject saveSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public Optional<Course> findCourseDetail(UUID courseId) {
        return courseRepository.findAcademicCourseDetail(courseId);
    }

    public Optional<Batch> findActiveBatchByBranchAndCourse(UUID branchId, UUID courseId) {
        return batchRepository.findFirstByBranch_IdAndCourse_IdAndIsActiveTrueAndIsDeletedFalseOrderByCreatedAtAsc(branchId, courseId);
    }

    public List<Batch> findActiveBatchesByCourse(UUID courseId) {
        return batchRepository.findByCourse_IdAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(courseId);
    }

    public Optional<Branch> findBranch(UUID branchId) {
        return branchRepository.findByIdAndIsDeletedFalse(branchId);
    }

    public Optional<Branch> findFirstActiveBranch() {
        return branchRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc().stream().findFirst();
    }

    public long countBatches() {
        return batchRepository.count();
    }

    public boolean existsBatchDisplayCode(String displayCode) {
        return batchRepository.existsByDisplayCode(displayCode);
    }

    public long countSubjects() {
        return subjectRepository.count();
    }

    public boolean existsSubjectCode(String subjectCode) {
        return subjectRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(subjectCode);
    }

    public boolean existsSubjectDisplayCode(String displayCode) {
        return subjectRepository.existsByDisplayCodeAndIsDeletedFalse(displayCode);
    }
}

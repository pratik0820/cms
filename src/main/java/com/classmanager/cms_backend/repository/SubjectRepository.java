package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Subject;
import com.classmanager.cms_backend.enums.SubjectCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc();

    Optional<Subject> findByCodeAndIsDeletedFalse(SubjectCode code);

    Optional<Subject> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByCodeAndIsDeletedFalse(SubjectCode code);
}

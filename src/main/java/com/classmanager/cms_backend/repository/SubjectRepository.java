package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAsc();

    Optional<Subject> findByCodeIgnoreCaseAndIsDeletedFalse(String code);

    Optional<Subject> findByIdAndIsDeletedFalse(UUID id);

    Optional<Subject> findByIdAndIsActiveTrueAndIsDeletedFalse(UUID id);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);
}

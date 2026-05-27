package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.FeeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeTransactionRepository extends JpaRepository<FeeTransaction, UUID> {
    List<FeeTransaction> findByEnrolment_IdAndIsDeletedFalseOrderByTransactionDateDesc(UUID enrolmentId);
}

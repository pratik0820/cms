package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.OperationalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OperationalRecordRepository extends JpaRepository<OperationalRecord, UUID> {

    List<OperationalRecord> findByModuleAndIsDeletedFalseOrderByCreatedAtDesc(String module);

    List<OperationalRecord> findByModuleAndEventDateBetweenAndIsDeletedFalseOrderByEventDateAscCreatedAtAsc(
            String module, LocalDate fromDate, LocalDate toDate);

    List<OperationalRecord> findTop10ByModuleAndIsDeletedFalseOrderByCreatedAtDesc(String module);
}

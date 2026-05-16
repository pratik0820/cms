package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.LeadFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadFollowUpRepository extends JpaRepository<LeadFollowUp, UUID> {

    List<LeadFollowUp> findByLead_IdAndIsDeletedFalseOrderByFollowUpAtDesc(UUID leadId);
}

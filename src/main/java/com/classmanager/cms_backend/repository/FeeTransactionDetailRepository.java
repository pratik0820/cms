package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.FeeTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeeTransactionDetailRepository extends JpaRepository<FeeTransactionDetail, UUID> {
}

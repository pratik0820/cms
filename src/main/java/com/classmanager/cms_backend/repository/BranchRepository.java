package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    List<Branch> findByIsDeletedFalseOrderByNameAsc();

    Optional<Branch> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    @Query("""
            select b
            from Branch b
            where b.isDeleted = false
              and (
                    :searchPattern is null
                    or lower(b.name) like :searchPattern
                    or lower(coalesce(b.city, '')) like :searchPattern
                    or lower(coalesce(b.phone, '')) like :searchPattern
                    or lower(coalesce(b.email, '')) like :searchPattern
              )
              and (:isActive is null or b.isActive = :isActive)
            """)
    Page<Branch> searchBranches(@Param("searchPattern") String searchPattern,
                                @Param("isActive") Boolean isActive,
                                Pageable pageable);

    @Query("""
            select count(b)
            from Branch b
            where b.isDeleted = false
              and (:isActive is null or b.isActive = :isActive)
            """)
    long countByIsDeletedFalseAndOptionalStatus(@Param("isActive") Boolean isActive);
}

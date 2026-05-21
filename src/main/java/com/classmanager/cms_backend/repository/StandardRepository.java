package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Standard;
import com.classmanager.cms_backend.enums.BoardType;
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
public interface StandardRepository extends JpaRepository<Standard, UUID> {

    Optional<Standard> findByIdAndIsDeletedFalse(UUID id);

    Optional<Standard> findByNameIgnoreCaseAndBoardAndIsDeletedFalse(String name, BoardType board);

    boolean existsByNameIgnoreCaseAndBoardAndIsDeletedFalse(String name, BoardType board);

    boolean existsByCodeAndIsDeletedFalse(String code);

    @Query("""
            select s from Standard s
            where s.isDeleted = false
              and (
                    cast(:searchPattern as string) is null
                    or lower(coalesce(s.name, '')) like cast(:searchPattern as string)
                    or lower(coalesce(cast(s.board as string), '')) like cast(:searchPattern as string)
                    or lower(coalesce(s.code, '')) like cast(:searchPattern as string)
                  )
            order by s.sortOrder asc, s.name asc
            """)
    Page<Standard> search(@Param("searchPattern") String searchPattern, Pageable pageable);

    List<Standard> findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAscNameAsc();
}

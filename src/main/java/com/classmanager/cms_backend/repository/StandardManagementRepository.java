package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Standard;
import com.classmanager.cms_backend.enums.BoardType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StandardManagementRepository {

    private final StandardRepository standardRepository;

    public Page<Standard> searchStandards(String searchPattern, Pageable pageable) {
        return standardRepository.search(searchPattern, pageable);
    }

    public List<Standard> findActiveStandardOptions() {
        return standardRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrderAscNameAsc();
    }

    public boolean existsByNameAndBoard(String standardName, BoardType board) {
        return standardRepository.existsByNameIgnoreCaseAndBoardAndIsDeletedFalse(standardName, board);
    }

    public Optional<Standard> findById(UUID standardId) {
        return standardRepository.findByIdAndIsDeletedFalse(standardId);
    }

    public Optional<Standard> findByNameAndBoard(String standardName, BoardType board) {
        return standardRepository.findByNameIgnoreCaseAndBoardAndIsDeletedFalse(standardName, board);
    }

    public Standard save(Standard standard) {
        return standardRepository.save(standard);
    }

    public long count() {
        return standardRepository.count();
    }

    public boolean existsByCode(String code) {
        return standardRepository.existsByCodeAndIsDeletedFalse(code);
    }
}

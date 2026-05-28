package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.request.CreateStandardRequest;
import com.classmanager.cms_backend.dto.request.UpdateStandardRequest;
import com.classmanager.cms_backend.dto.response.BoardOptionResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.StandardResponse;
import com.classmanager.cms_backend.entity.Standard;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.exception.BadRequestException;
import com.classmanager.cms_backend.exception.ResourceAlreadyExistsException;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.StandardManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StandardService {

    private final StandardManagementRepository standardManagementRepository;

    @Transactional(readOnly = true)
    public List<BoardOptionResponse> listBoards() {
        return Arrays.stream(BoardType.values())
                .map(board -> BoardOptionResponse.builder()
                        .code(board)
                        .label(board.getDisplayName())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<StandardResponse> listStandards(String search, int page, int size) {
        Page<StandardResponse> result = standardManagementRepository.searchStandards(
                        buildSearchPattern(search),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder", "name")))
                .map(this::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<StandardResponse> listStandardOptions() {
        return standardManagementRepository.findActiveStandardOptions().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StandardResponse createStandard(CreateStandardRequest request) {
        String standardName = normalizeRequired(request.getStandard(), "Class / standard is required");
        if (standardManagementRepository.existsByNameAndBoard(standardName, request.getBoard())) {
            throw new ResourceAlreadyExistsException("This standard and board combination already exists.");
        }

        Standard standard = Standard.builder()
                .code(nextCode("STD", standardManagementRepository.count() + 1))
                .name(standardName)
                .board(request.getBoard())
                .sortOrder((int) standardManagementRepository.count())
                .isActive(true)
                .build();
        return toResponse(standardManagementRepository.save(standard));
    }

    @Transactional
    public StandardResponse updateStandard(UUID standardId, UpdateStandardRequest request) {
        Standard standard = standardManagementRepository.findById(standardId)
                .orElseThrow(() -> new ResourceNotFoundException("Standard", standardId));

        String standardName = normalizeRequired(request.getStandard(), "Class / standard is required");
        standardManagementRepository.findByNameAndBoard(standardName, request.getBoard())
                .filter(existing -> !existing.getId().equals(standardId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("This standard and board combination already exists.");
                });

        standard.setName(standardName);
        standard.setBoard(request.getBoard());
        return toResponse(standardManagementRepository.save(standard));
    }

    @Transactional
    public void deleteStandard(UUID standardId) {
        Standard standard = standardManagementRepository.findById(standardId)
                .orElseThrow(() -> new ResourceNotFoundException("Standard", standardId));
        standard.softDelete();
        standard.setIsActive(false);
        standardManagementRepository.save(standard);
    }

    private StandardResponse toResponse(Standard standard) {
        return StandardResponse.builder()
                .id(standard.getId())
                .standardId(standard.getCode())
                .standard(standard.getName())
                .board(standard.getBoard())
                .boardLabel(standard.getBoard().getDisplayName())
                .status(Boolean.TRUE.equals(standard.getIsActive()) ? "Active" : "Inactive")
                .build();
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message, "VALIDATION_ERROR");
        }
        return value.trim();
    }

    private String buildSearchPattern(String value) {
        return StringUtils.hasText(value) ? "%" + value.trim().toLowerCase(Locale.ENGLISH) + "%" : null;
    }

    private String nextCode(String prefix, long startAt) {
        long index = Math.max(startAt, 1);
        String candidate;
        do {
            candidate = prefix + String.format("%04d", index);
            index++;
        } while (standardManagementRepository.existsByCode(candidate));
        return candidate;
    }
}

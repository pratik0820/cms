package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.entity.Batch;
import com.classmanager.cms_backend.entity.Branch;
import com.classmanager.cms_backend.enums.BoardType;
import com.classmanager.cms_backend.exception.ResourceNotFoundException;
import com.classmanager.cms_backend.repository.BatchRepository;
import com.classmanager.cms_backend.repository.BranchRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "Branch and batch/class master data")
public class MasterDataController extends BaseController {

    private final BranchRepository branchRepository;
    private final BatchRepository batchRepository;

    @GetMapping("/branches")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Branch>>> listBranches() {
        return ResponseEntity.ok(ApiResponse.success(
                branchRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(currentTenantId())));
    }

    @PostMapping("/branches")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Branch>> createBranch(@RequestBody BranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .phone(request.getPhone())
                .email(request.getEmail())
                .isActive(true)
                .build();
        branch.setTenantId(currentTenantId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(branchRepository.save(branch), "Branch created."));
    }

    @GetMapping("/batches")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Batch>>> listBatches(@RequestParam(required = false) UUID branchId) {
        UUID tenantId = currentTenantId();
        List<Batch> batches = branchId == null
                ? batchRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(tenantId)
                : batchRepository.findByTenantIdAndBranchIdAndIsDeletedFalseOrderByNameAsc(tenantId, branchId);
        return ResponseEntity.ok(ApiResponse.success(batches));
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Batch>> createBatch(@RequestBody BatchRequest request) {
        UUID tenantId = currentTenantId();
        Branch branch = branchRepository.findByIdAndTenantIdAndIsDeletedFalse(request.getBranchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        Batch batch = Batch.builder()
                .branch(branch)
                .name(request.getName())
                .standard(request.getStandard())
                .board(request.getBoard())
                .academicYear(request.getAcademicYear())
                .maxStudents(request.getMaxStudents())
                .isActive(true)
                .build();
        batch.setTenantId(tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(batchRepository.save(batch), "Batch created."));
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Batch>> getBatch(@PathVariable UUID id) {
        Batch batch = batchRepository.findByIdAndTenantIdAndIsDeletedFalse(id, currentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
        return ResponseEntity.ok(ApiResponse.success(batch));
    }

    @Data
    public static class BranchRequest {
        private String name;
        private String address;
        private String city;
        private String phone;
        private String email;
    }

    @Data
    public static class BatchRequest {
        private UUID branchId;
        private String name;
        private String standard;
        private BoardType board;
        private String academicYear;
        private Integer maxStudents;
    }
}

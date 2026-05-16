package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateBatchRequest;
import com.classmanager.cms_backend.dto.request.UpdateBatchRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.BatchResponse;
import com.classmanager.cms_backend.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Tag(name = "Batches", description = "Batch management — create, list, update and delete batches")
public class BatchController extends BaseController {

    private final BatchService batchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a new batch for a branch and course")
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(
            @Valid @RequestBody CreateBatchRequest request) {
        BatchResponse response = batchService.createBatch(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Batch created successfully"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List batches (paginated). Filter by branchId.")
    public ResponseEntity<ApiResponse<Page<BatchResponse>>> listBatches(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BatchResponse> result = batchService.listBatches(
                branchId, PageRequest.of(page, size, Sort.by("name")));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/by-branch/{branchId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all active batches for a branch (no pagination — for dropdowns)")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> listBatchesByBranch(
            @PathVariable UUID branchId,
            @RequestParam(required = false) String academicYear) {
        List<BatchResponse> result = academicYear != null
                ? batchService.listBatchesByBranchAndYear(branchId, academicYear)
                : batchService.listBatchesByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get batch details")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatch(@PathVariable UUID batchId) {
        return ResponseEntity.ok(ApiResponse.success(batchService.getBatch(batchId)));
    }

    @PutMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update batch details")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable UUID batchId,
            @RequestBody UpdateBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(batchService.updateBatch(batchId, request), "Batch updated"));
    }

    @DeleteMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Soft-delete a batch")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable UUID batchId) {
        batchService.deleteBatch(batchId);
        return ResponseEntity.ok(ApiResponse.success(null, "Batch deleted"));
    }
}

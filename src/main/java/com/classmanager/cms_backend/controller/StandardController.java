package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.dto.request.CreateStandardRequest;
import com.classmanager.cms_backend.dto.request.UpdateStandardRequest;
import com.classmanager.cms_backend.dto.response.ApiResponse;
import com.classmanager.cms_backend.dto.response.BoardOptionResponse;
import com.classmanager.cms_backend.dto.response.PagedResponse;
import com.classmanager.cms_backend.dto.response.StandardResponse;
import com.classmanager.cms_backend.service.StandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Tag(name = "Standards", description = "Standards and boards APIs")
public class StandardController extends BaseController {

    private final StandardService standardService;

    @GetMapping("/boards")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List available boards")
    public ResponseEntity<ApiResponse<List<BoardOptionResponse>>> listBoards() {
        return ResponseEntity.ok(ApiResponse.success(standardService.listBoards()));
    }

    @GetMapping("/standards")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List standards")
    public ResponseEntity<ApiResponse<PagedResponse<StandardResponse>>> listStandards(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(standardService.listStandards(search, page, size)));
    }

    @GetMapping("/standards/options")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active standards for dropdowns")
    public ResponseEntity<ApiResponse<List<StandardResponse>>> listStandardOptions() {
        return ResponseEntity.ok(ApiResponse.success(standardService.listStandardOptions()));
    }

    @PostMapping("/standards")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Create a standard")
    public ResponseEntity<ApiResponse<StandardResponse>> createStandard(@Valid @RequestBody CreateStandardRequest request) {
        return ResponseEntity.ok(ApiResponse.success(standardService.createStandard(request), "Standard created successfully"));
    }

    @PutMapping("/standards/{standardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Update a standard")
    public ResponseEntity<ApiResponse<StandardResponse>> updateStandard(@PathVariable UUID standardId,
                                                                        @Valid @RequestBody UpdateStandardRequest request) {
        return ResponseEntity.ok(ApiResponse.success(standardService.updateStandard(standardId, request), "Standard updated successfully"));
    }

    @DeleteMapping("/standards/{standardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(summary = "Delete a standard")
    public ResponseEntity<ApiResponse<Void>> deleteStandard(@PathVariable UUID standardId) {
        standardService.deleteStandard(standardId);
        return ResponseEntity.ok(ApiResponse.success(null, "Standard deleted successfully"));
    }
}

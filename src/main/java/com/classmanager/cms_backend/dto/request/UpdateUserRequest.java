package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Request body for PUT /api/users/{id}
 * All fields are optional — only non-null fields are updated (PATCH semantics).
 */
@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 150, message = "Full name must be 2–150 characters")
    private String fullName;

    @Email(message = "Please provide a valid email address")
    private String email;

    private String phone;

    private UUID branchId;
}

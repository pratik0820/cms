package com.classmanager.cms_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    private String address;

    private String city;

    private String phone;

    @Email(message = "Please provide a valid email address")
    private String email;
}

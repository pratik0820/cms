package com.classmanager.cms_backend.dto.request;

import com.classmanager.cms_backend.enums.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStandardRequest {

    @NotBlank(message = "Class / standard is required")
    private String standard;

    @NotNull(message = "Board is required")
    private BoardType board;
}

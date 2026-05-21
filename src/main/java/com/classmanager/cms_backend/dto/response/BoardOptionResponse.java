package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.BoardType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardOptionResponse {
    private BoardType code;
    private String label;
}

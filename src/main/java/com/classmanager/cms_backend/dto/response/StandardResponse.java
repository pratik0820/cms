package com.classmanager.cms_backend.dto.response;

import com.classmanager.cms_backend.enums.BoardType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StandardResponse {
    private UUID id;
    private String standardId;
    private String standard;
    private BoardType board;
    private String boardLabel;
    private String status;
}

package com.classmanager.cms_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminManagementResponse {

    private Summary summary;
    private List<AdminResponse> content;
    private PageMeta page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalAdmins;
        private long activeAdmins;
        private long inactiveAdmins;
        private long allBranchAdmins;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMeta {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}

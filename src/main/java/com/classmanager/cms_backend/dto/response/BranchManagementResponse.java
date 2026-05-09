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
public class BranchManagementResponse {

    private Summary summary;
    private List<BranchResponse> content;
    private PageMeta page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalBranches;
        private long activeBranches;
        private long inactiveBranches;
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

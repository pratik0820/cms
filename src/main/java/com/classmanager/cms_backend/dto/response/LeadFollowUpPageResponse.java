package com.classmanager.cms_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeadFollowUpPageResponse {

    private Summary summary;
    private List<LeadFollowUpResponse> content;
    private PageMeta page;

    @Data
    @Builder
    public static class Summary {
        private long totalFollowUps;
        private long completed;
        private long pending;
        private String conversionRate;
    }

    @Data
    @Builder
    public static class PageMeta {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}

package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SuperAdminAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SuperAdminAnalyticsService superAdminAnalyticsService;

    public SuperAdminAnalyticsResponse getAnalytics(String tab, LocalDate fromDate, LocalDate toDate, UUID branchId) {
        return superAdminAnalyticsService.getAnalytics(tab, fromDate, toDate, branchId);
    }
}

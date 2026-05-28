package com.classmanager.cms_backend.service;

import com.classmanager.cms_backend.dto.response.SuperAdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SuperAdminDashboardService superAdminDashboardService;

    public SuperAdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, UUID branchId) {
        return superAdminDashboardService.getDashboard(fromDate, toDate, branchId);
    }
}

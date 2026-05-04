package com.classmanager.cms_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlanConfig {
    private int maxStudents;
    private int maxBranches;
}

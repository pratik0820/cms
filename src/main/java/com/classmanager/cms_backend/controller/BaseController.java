package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.security.CmsUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public abstract class BaseController {

    protected CmsUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CmsUserDetails userDetails) {
            return userDetails;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }

    protected UUID currentUserId() {
        return currentUser().getUserId();
    }

    protected UUID currentBranchId() {
        return currentUser().getBranchId();
    }
}

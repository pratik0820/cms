package com.classmanager.cms_backend.controller;

import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.security.CmsUserDetails;
import com.classmanager.cms_backend.service.BaseService;
import com.classmanager.cms_backend.tenant.TenantContext;
import com.classmanager.cms_backend.util.ValidatorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public abstract class BaseController extends BaseService {

    private static final Logger log = LogManager.getLogger(BaseController.class);

    protected Optional<User> fetchUser(String input) {

        log.info("-----fetching user based on the login input for email or phone: {}", input);

        log.info("----- checking input against email pattern");
        if (ValidatorUtil.isValidEmail(input)) {
            log.info("----- input matches with email, hence fetching user based on email");
            return userService.findByUserEmail(input);
        }
        log.info("----- input is not an email");

        log.info("----- checking input against phone number");
        if (ValidatorUtil.isNumeric(input, 10)) {
            log.info("----- input matches with phone number, hence fetching user based on phone number");
            return userService.findByUserPhone(input);
        }
        log.info("----- input is not phone number as well, invalid input: {}", input);
        return Optional.empty();
    }

    protected CmsUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CmsUserDetails ud) {
            return ud;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }

    protected UUID currentUserId() {
        return currentUser().getUserId();
    }

    protected UUID currentTenantId() {
        UUID tid = TenantContext.getCurrentTenant();
        return tid != null ? tid : currentUser().getTenantId();
    }

    protected UUID currentBranchId() {
        return currentUser().getBranchId();
    }
}

package com.classmanager.cms_backend.security;

import com.classmanager.cms_backend.repository.UserRepository;
import com.classmanager.cms_backend.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CmsUserDetailsService implements UserDetailsService {

    private static final Logger log = LogManager.getLogger(CmsUserDetailsService.class);
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrLoginId) throws UsernameNotFoundException {
        var user = TenantContext.hasTenant()
                ? userRepository.findByEmailAndTenantIdAndIsDeletedFalse(emailOrLoginId, TenantContext.getCurrentTenant())
                    .or(() -> userRepository.findStudentUserByLoginIdAndTenantId(emailOrLoginId, TenantContext.getCurrentTenant()))
                : userRepository.findByEmailAndIsDeletedFalse(emailOrLoginId);

        return user
                .map(CmsUserDetails::new)
                .orElseThrow(() -> {
                    log.warn("User not found with identifier: {}", emailOrLoginId);
                    // Generic message to prevent user enumeration attacks
                    return new UsernameNotFoundException("Invalid credentials");
                });
    }
}

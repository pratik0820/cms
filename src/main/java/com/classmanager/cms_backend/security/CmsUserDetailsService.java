package com.classmanager.cms_backend.security;

import com.classmanager.cms_backend.repository.UserRepository;
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
        String normalized = emailOrLoginId == null ? "" : emailOrLoginId.trim().toLowerCase();
        var user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(normalized)
                .or(() -> userRepository.findByLoginIdIgnoreCaseAndIsDeletedFalse(normalized));

        return user
                .map(CmsUserDetails::new)
                .orElseThrow(() -> {
                    log.warn("User not found with identifier: {}", emailOrLoginId);
                    // Generic message to prevent user enumeration attacks
                    return new UsernameNotFoundException("Invalid credentials");
                });
    }
}

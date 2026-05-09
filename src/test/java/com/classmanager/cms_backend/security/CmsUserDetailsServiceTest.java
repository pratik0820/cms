package com.classmanager.cms_backend.security;

import com.classmanager.cms_backend.entity.User;
import com.classmanager.cms_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CmsUserDetailsService cmsUserDetailsService;

    @Test
    void loadsUserByEmailWithoutTenantContext() {
        User user = User.builder()
                .email("teacher@example.com")
                .passwordHash("hash")
                .fullName("Teacher")
                .isActive(true)
                .build();

        when(userRepository.findByEmailIgnoreCaseAndIsDeletedFalse("teacher@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = cmsUserDetailsService.loadUserByUsername("teacher@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("teacher@example.com");
        verify(userRepository).findByEmailIgnoreCaseAndIsDeletedFalse("teacher@example.com");
    }
}

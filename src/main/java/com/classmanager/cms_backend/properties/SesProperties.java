package com.classmanager.cms_backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws.ses")
public class SesProperties {

    private boolean enabled = false;
    private String fromEmail;
}

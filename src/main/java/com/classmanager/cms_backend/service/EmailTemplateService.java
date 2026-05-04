package com.classmanager.cms_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailTemplateService {

    private final ResourceLoader resourceLoader;
    private final String basePath;

    public EmailTemplateService(ResourceLoader resourceLoader,
                                @Value("${email.templates.base-path:classpath:/templates}") String basePath) {
        this.resourceLoader = resourceLoader;
        this.basePath = basePath;
    }

    public String render(String templatePath, Map<String, String> variables) {
        String template = loadTemplate(templatePath);
        String rendered = template;

        for (Map.Entry<String, String> e : variables.entrySet()) {
            rendered = rendered.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
        }

        return rendered;
    }

    private String loadTemplate(String templateName) {
        String location = basePath.endsWith("/") ? basePath + templateName : basePath + "/" + templateName;
        Resource resource = resourceLoader.getResource(location);

        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load template: " + location, ex);
        }
    }
}

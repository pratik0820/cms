package com.classmanager.cms_backend.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class TenantFilterAspect {

    private static final Logger log = LogManager.getLogger(TenantFilterAspect.class);
    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.classmanager.cms_backend.repository..*(..)) || " +
            "execution(* com.classmanager.cms_backend.service..*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.getEnabledFilter("tenantFilter");
                if (filter == null) {
                    session.enableFilter("tenantFilter")
                            .setParameter("tenantId", tenantId);
                }
            } catch (Exception e) {
                log.debug("Could not enable tenant filter: {}", e.getMessage());
            }
        }
    }
}

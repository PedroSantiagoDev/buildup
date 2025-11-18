package com.maistech.buildup.tenant;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(
        TenantInterceptor.class
    );

    private final EntityManager entityManager;

    public TenantInterceptor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        var tenantId = TenantContext.getTenantId();

        if (tenantId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.getEnabledFilter("tenantFilter");

                if (filter == null) {
                    filter = session.enableFilter("tenantFilter");
                }

                if (filter != null) {
                    filter.setParameter("tenantId", tenantId);
                    log.debug("Tenant filter enabled for tenantId: {}", tenantId);
                }
            } catch (IllegalArgumentException e) {
                log.trace("Tenant filter not available: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("Failed to enable tenant filter: {}", e.getMessage());
            }
        }

        return true;
    }
}

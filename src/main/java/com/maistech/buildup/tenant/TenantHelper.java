package com.maistech.buildup.tenant;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class TenantHelper {

    private final EntityManager entityManager;

    public TenantHelper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> T withoutTenantFilter(Supplier<T> action) {
        Session session = entityManager.unwrap(Session.class);

        boolean wasEnabled = session.getEnabledFilter("tenantFilter") != null;

        if (wasEnabled) {
            session.disableFilter("tenantFilter");
        }

        try {
            return action.get();
        } finally {
            if (wasEnabled) {
                var tenantId = TenantContext.getTenantId();
                if (tenantId != null) {
                    session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
                }
            }
        }
    }

    public void withoutTenantFilter(Runnable action) {
        withoutTenantFilter(() -> {
            action.run();
            return null;
        });
    }
}

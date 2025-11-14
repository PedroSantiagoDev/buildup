package com.maistech.buildup.shared.tenant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.UUID;

public class TenantListener {

    @PrePersist
    public void setTenantOnCreate(Object entity) {
        if (entity instanceof TenantAware tenantAware) {
            UUID tenantId = TenantContext.getTenantId();
            if (tenantId != null && tenantAware.getCompanyId() == null) {
                tenantAware.setCompanyId(tenantId);
            }
        }
    }

    @PreUpdate
    public void validateTenantOnUpdate(Object entity) {
        if (entity instanceof TenantAware tenantAware) {
            UUID currentTenant = TenantContext.getTenantId();
            UUID entityTenant = tenantAware.getCompanyId();

            if (
                currentTenant != null &&
                entityTenant != null &&
                !currentTenant.equals(entityTenant)
            ) {
                throw new SecurityException(
                    "Attempted to modify entity from different tenant"
                );
            }
        }
    }
}

package com.maistech.buildup.shared.multitenancy;

import java.util.UUID;

public interface TenantAware {
    UUID getCompanyId();
    void setCompanyId(UUID companyId);
}

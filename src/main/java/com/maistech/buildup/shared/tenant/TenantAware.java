package com.maistech.buildup.shared.tenant;

import java.util.UUID;

public interface TenantAware {
    UUID getCompanyId();
    void setCompanyId(UUID companyId);
}

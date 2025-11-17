/**
 * Shared Infrastructure Module.
 * 
 * Contains shared configurations, utilities, and cross-cutting concerns
 * that are used across all application modules.
 * 
 * Public API:
 * - {@link com.maistech.buildup.shared.config.*} - Configuration classes
 * - {@link com.maistech.buildup.shared.entity.BaseEntity} - Base entity for all domain entities
 * - {@link com.maistech.buildup.shared.tenant.*} - Multi-tenancy infrastructure
 * - {@link com.maistech.buildup.shared.exception.*} - Exception handling
 * 
 * This module is typically a dependency for all other business modules.
 */
@ApplicationModule(
    displayName = "Shared Infrastructure",
    allowedDependencies = {"auth", "company", "role"}  // Infrastructure needs auth/company/role for configs
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.shared;

import org.springframework.modulith.ApplicationModule;

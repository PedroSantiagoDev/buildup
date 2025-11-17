/**
 * Company Management Module.
 * 
 * Handles company registration, management, and multi-tenancy.
 * This is a core module that other business modules depend on.
 * 
 * Public API:
 * - {@link com.maistech.buildup.company.CompanyEntity} - Company entity
 * - {@link com.maistech.buildup.company.domain.CompanyRepository} - Company repository
 * 
 * Internal:
 * - {@link com.maistech.buildup.company.domain.CompanyService} - Company management service
 */
@ApplicationModule(
    displayName = "Company Management",
    allowedDependencies = {"auth", "role", "shared"}  // Can access auth, role and shared
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.company;

import org.springframework.modulith.ApplicationModule;

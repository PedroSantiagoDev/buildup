/**
 * Role Management Module.
 * 
 * Handles user roles and permissions. This is a foundational module.
 * 
 * Public API:
 * - {@link com.maistech.buildup.role.RoleEntity} - Role entity
 * - {@link com.maistech.buildup.role.RoleEnum} - Role enumeration
 * - {@link com.maistech.buildup.role.RoleRepository} - Role repository
 */
@ApplicationModule(
    displayName = "Role Management",
    allowedDependencies = {}  // Core module, no dependencies
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.role;

import org.springframework.modulith.ApplicationModule;

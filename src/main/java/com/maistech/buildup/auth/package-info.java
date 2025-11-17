/**
 * Authentication and User Management Module.
 * 
 * Handles user authentication, authorization, and user entity management.
 * This module is fundamental and should not depend on business domain modules.
 * 
 * Public API:
 * - {@link com.maistech.buildup.auth.UserEntity} - User entity (accessed by other modules)
 * - {@link com.maistech.buildup.auth.domain.UserRepository} - User repository (for queries)
 * 
 * Internal:
 * - {@link com.maistech.buildup.auth.domain.AuthService} - Authentication service
 */
@ApplicationModule(
    displayName = "Authentication & Users",
    allowedDependencies = {"role", "company", "shared"}  // Auth needs role and company for registration
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.auth;

import org.springframework.modulith.ApplicationModule;

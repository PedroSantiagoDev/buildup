/**
 * Project Management Module.
 * 
 * Manages construction projects, including members, status, and financial tracking.
 * 
 * Public API:
 * - {@link com.maistech.buildup.project.ProjectEntity} - Project entity
 * - {@link com.maistech.buildup.project.ProjectMemberEntity} - Project member entity
 * - {@link com.maistech.buildup.project.domain.ProjectRepository} - Project repository
 * 
 * Internal:
 * - {@link com.maistech.buildup.project.domain.ProjectService} - Project business logic
 * - {@link com.maistech.buildup.project.domain.ProjectMemberRepository} - Member repository
 */
@ApplicationModule(
    displayName = "Project Management",
    allowedDependencies = {"auth", "company", "shared"}
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.project;

import org.springframework.modulith.ApplicationModule;

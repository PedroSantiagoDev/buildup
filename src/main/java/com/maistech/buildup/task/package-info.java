/**
 * Task Management Module.
 * 
 * Manages project tasks, dependencies, progress tracking, and scheduling.
 * 
 * Public API:
 * - {@link com.maistech.buildup.task.TaskEntity} - Task entity
 * - {@link com.maistech.buildup.task.TaskDependencyEntity} - Task dependency entity
 * - {@link com.maistech.buildup.task.domain.TaskRepository} - Task repository
 * 
 * Internal:
 * - {@link com.maistech.buildup.task.domain.TaskService} - Task business logic
 * - {@link com.maistech.buildup.task.domain.TaskDependencyRepository} - Dependency repository
 */
@ApplicationModule(
    displayName = "Task Management",
    allowedDependencies = {"auth", "project", "shared"}  // Tasks belong to projects
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.task;

import org.springframework.modulith.ApplicationModule;

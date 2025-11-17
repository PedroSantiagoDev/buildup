/**
 * Schedule Management Module.
 * 
 * Manages project schedules, milestones, and timeline planning.
 * 
 * Public API:
 * - {@link com.maistech.buildup.schedule.ScheduleEntity} - Schedule entity
 * - {@link com.maistech.buildup.schedule.MilestoneEntity} - Milestone entity
 * - {@link com.maistech.buildup.schedule.domain.ScheduleRepository} - Schedule repository
 * 
 * Internal:
 * - {@link com.maistech.buildup.schedule.domain.ScheduleService} - Schedule business logic
 * - {@link com.maistech.buildup.schedule.domain.MilestoneRepository} - Milestone repository
 */
@ApplicationModule(
    displayName = "Schedule Management",
    allowedDependencies = {"auth", "project", "task", "shared"}  // Schedules relate to projects and tasks
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.schedule;

import org.springframework.modulith.ApplicationModule;

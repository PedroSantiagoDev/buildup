package com.maistech.buildup.schedule;

import com.maistech.buildup.schedule.dto.*;
import com.maistech.buildup.schedule.domain.ScheduleService;
import com.maistech.buildup.shared.security.JWTUserData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/schedule")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Schedule & Milestones",
    description = "Project schedule management including timeline, milestones, and progress tracking. SUPER_ADMIN can optionally specify companyId via query parameter."
)
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Generate project schedule",
        description = "Generates or regenerates the project schedule based on tasks and their dependencies. Calculates timeline, progress, and critical path. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Schedule generated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleResponse.class)
                )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            ),
        }
    )
    public ResponseEntity<ScheduleResponse> generateSchedule(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ScheduleResponse schedule = scheduleService.generateSchedule(
            targetCompanyId,
            projectId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get project schedule",
        description = "Returns the project schedule with timeline, progress, milestones, and calculated fields. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Schedule retrieved successfully"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            ),
        }
    )
    public ResponseEntity<ScheduleResponse> getSchedule(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ScheduleResponse schedule = scheduleService.getScheduleByProjectId(
            targetCompanyId,
            projectId
        );

        return ResponseEntity.ok(schedule);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update schedule",
        description = "Updates schedule information such as dates, status, and notes. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Schedule updated"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            ),
        }
    )
    public ResponseEntity<ScheduleResponse> updateSchedule(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody UpdateScheduleRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ScheduleResponse schedule = scheduleService.updateSchedule(
            targetCompanyId,
            projectId,
            request
        );

        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Recalculate schedule",
        description = "Recalculates schedule metrics based on current task status. Use after updating tasks. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Schedule recalculated"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            ),
        }
    )
    public ResponseEntity<ScheduleResponse> recalculateSchedule(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ScheduleResponse schedule = scheduleService.recalculateSchedule(
            targetCompanyId,
            projectId
        );

        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/milestones")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create milestone",
        description = "Creates a new milestone for the project schedule. Milestones mark important dates and deliverables. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Milestone created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            ),
        }
    )
    public ResponseEntity<MilestoneResponse> createMilestone(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody CreateMilestoneRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        MilestoneResponse milestone = scheduleService.createMilestone(
            targetCompanyId,
            projectId,
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(milestone);
    }

    @GetMapping("/milestones")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List project milestones",
        description = "Returns all milestones for the project ordered by planned date. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Milestones retrieved"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public ResponseEntity<List<MilestoneResponse>> listMilestones(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        List<MilestoneResponse> milestones =
            scheduleService.listProjectMilestones(targetCompanyId, projectId);

        return ResponseEntity.ok(milestones);
    }

    @PutMapping("/milestones/{milestoneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update milestone",
        description = "Updates milestone information including dates, status, and completion percentage. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Milestone updated"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Milestone not found"
            ),
        }
    )
    public ResponseEntity<MilestoneResponse> updateMilestone(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Milestone ID") @PathVariable UUID milestoneId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody UpdateMilestoneRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        MilestoneResponse milestone = scheduleService.updateMilestone(
            targetCompanyId,
            projectId,
            milestoneId,
            request
        );

        return ResponseEntity.ok(milestone);
    }

    @DeleteMapping("/milestones/{milestoneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Delete milestone",
        description = "Permanently deletes a milestone from the project schedule. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Milestone deleted"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Milestone not found"
            ),
        }
    )
    public ResponseEntity<Void> deleteMilestone(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Milestone ID") @PathVariable UUID milestoneId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        scheduleService.deleteMilestone(targetCompanyId, projectId, milestoneId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/milestones/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "List overdue milestones",
        description = "Returns all milestones that are past their planned date and not completed. SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Overdue milestones retrieved"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public ResponseEntity<List<MilestoneResponse>> listOverdueMilestones(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        List<MilestoneResponse> milestones =
            scheduleService.listOverdueMilestones(targetCompanyId);

        return ResponseEntity.ok(milestones);
    }

    @GetMapping("/milestones/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List upcoming milestones",
        description = "Returns milestones scheduled within the next N days (default 30). SUPER_ADMIN can optionally specify companyId via query parameter."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Upcoming milestones retrieved"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
        }
    )
    public ResponseEntity<List<MilestoneResponse>> listUpcomingMilestones(
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Parameter(description = "Number of days to look ahead") @RequestParam(
            required = false,
            defaultValue = "30"
        ) Integer days,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        List<MilestoneResponse> milestones =
            scheduleService.listUpcomingMilestones(targetCompanyId, days);

        return ResponseEntity.ok(milestones);
    }

    private UUID getTargetCompanyId(Authentication authentication, UUID requestedCompanyId) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        
        if (requestedCompanyId != null) {
            if (!userData.isMasterCompany()) {
                throw new IllegalStateException(
                    "Only SUPER_ADMIN can access other companies' resources"
                );
            }
            return requestedCompanyId;
        }
        
        return userData.companyId();
    }
}

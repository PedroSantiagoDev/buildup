package com.maistech.buildup.schedule;

import com.maistech.buildup.schedule.domain.PhaseService;
import com.maistech.buildup.schedule.dto.CreatePhaseRequest;
import com.maistech.buildup.schedule.dto.PhaseResponse;
import com.maistech.buildup.schedule.dto.UpdatePhaseRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules/{scheduleId}/phases")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Phases",
    description = "Phase management for project schedules. Phases organize tasks into logical groups (e.g., Foundation, Structure, Finishing). SUPER_ADMIN can optionally specify companyId via query parameter."
)
public class PhaseController {

    private final PhaseService phaseService;

    public PhaseController(PhaseService phaseService) {
        this.phaseService = phaseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create phase",
        description = "Creates a new phase in the schedule. Phases help organize tasks into logical groups like Foundation, Structure, Finishing, etc."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Phase created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PhaseResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            ),
        }
    )
    public ResponseEntity<PhaseResponse> createPhase(
        @Parameter(description = "Schedule ID") @PathVariable UUID scheduleId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody CreatePhaseRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        PhaseResponse phase = phaseService.createPhase(
            targetCompanyId,
            scheduleId,
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(phase);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List schedule phases",
        description = "Returns all phases for the schedule ordered by index"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Phases retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Schedule not found"
            ),
        }
    )
    public ResponseEntity<List<PhaseResponse>> listPhases(
        @Parameter(description = "Schedule ID") @PathVariable UUID scheduleId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        List<PhaseResponse> phases = phaseService.listPhasesBySchedule(
            targetCompanyId,
            scheduleId
        );

        return ResponseEntity.ok(phases);
    }

    @GetMapping("/{phaseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get phase by ID",
        description = "Returns detailed information about a specific phase including tasks count and completion status"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Phase retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PhaseResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Phase not found"
            ),
        }
    )
    public ResponseEntity<PhaseResponse> getPhase(
        @Parameter(description = "Schedule ID") @PathVariable UUID scheduleId,
        @Parameter(description = "Phase ID") @PathVariable UUID phaseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        PhaseResponse phase = phaseService.getPhaseById(
            targetCompanyId,
            scheduleId,
            phaseId
        );

        return ResponseEntity.ok(phase);
    }

    @PutMapping("/{phaseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update phase",
        description = "Updates phase information. All fields are optional - only provided fields will be updated."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Phase updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PhaseResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Phase not found"
            ),
        }
    )
    public ResponseEntity<PhaseResponse> updatePhase(
        @Parameter(description = "Schedule ID") @PathVariable UUID scheduleId,
        @Parameter(description = "Phase ID") @PathVariable UUID phaseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody UpdatePhaseRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        PhaseResponse phase = phaseService.updatePhase(
            targetCompanyId,
            scheduleId,
            phaseId,
            request
        );

        return ResponseEntity.ok(phase);
    }

    @DeleteMapping("/{phaseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Delete phase",
        description = "Permanently deletes a phase. Warning: Tasks in this phase will also be affected."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Phase deleted successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Phase not found"
            ),
        }
    )
    public ResponseEntity<Void> deletePhase(
        @Parameter(description = "Schedule ID") @PathVariable UUID scheduleId,
        @Parameter(description = "Phase ID") @PathVariable UUID phaseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        phaseService.deletePhase(targetCompanyId, scheduleId, phaseId);

        return ResponseEntity.noContent().build();
    }

    private UUID getTargetCompanyId(
        Authentication authentication,
        UUID requestedCompanyId
    ) {
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

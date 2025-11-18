package com.maistech.buildup.project;

import com.maistech.buildup.project.dto.*;
import com.maistech.buildup.project.domain.ProjectService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies/{companyId}/projects")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Projects",
    description = "Project management endpoints for construction projects. Supports CRUD operations, member management, and project tracking."
)
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create new project",
        description = "Creates a new construction project. The authenticated user becomes the project creator and is automatically added as a member with 'Project Manager' role."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Project created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjectResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error - invalid request data"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Company not found"
            )
        }
    )
    public ResponseEntity<ProjectResponse> createProject(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Valid @RequestBody CreateProjectRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        ProjectResponse project = projectService.createProject(
            companyId,
            userData.userId(),
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List all projects",
        description = "Returns a paginated list of all projects within the company. Results are sorted by creation date (newest first) by default."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Projects retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user does not have access to this company"
            )
        }
    )
    public ResponseEntity<Page<ProjectResponse>> listProjects(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Pagination parameters (page, size, sort)")
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        Page<ProjectResponse> projects = projectService.listProjects(
            companyId,
            pageable
        );
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List user's projects",
        description = "Returns all projects where the authenticated user is a member."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "User's projects retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user does not have access to this company"
            )
        }
    )
    public ResponseEntity<List<ProjectResponse>> listMyProjects(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        List<ProjectResponse> projects = projectService.listUserProjects(
            companyId,
            userData.userId()
        );

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get project by ID",
        description = "Returns detailed information about a specific project including all members, financial data, and calculated fields."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Project retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjectResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user does not have access to this company"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            )
        }
    )
    public ResponseEntity<ProjectResponse> getProject(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ProjectResponse project = projectService.getProjectById(
            companyId,
            projectId
        );
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update project",
        description = "Updates project information. All fields in the request are optional - only provided fields will be updated."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Project updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjectResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            )
        }
    )
    public ResponseEntity<ProjectResponse> updateProject(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Valid @RequestBody UpdateProjectRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ProjectResponse project = projectService.updateProject(
            companyId,
            projectId,
            request
        );
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Delete project",
        description = "Permanently deletes a project and all associated data including members. This action cannot be undone."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Project deleted successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            )
        }
    )
    public ResponseEntity<Void> deleteProject(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        projectService.deleteProject(companyId, projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Add member to project",
        description = "Adds a user as a member of the project with a specified role and edit permissions. User must belong to the same company."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Member added successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjectMemberResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error or user already a member"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project or user not found"
            )
        }
    )
    public ResponseEntity<ProjectMemberResponse> addMember(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Valid @RequestBody AddMemberRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ProjectMemberResponse member = projectService.addMember(
            companyId,
            projectId,
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping("/{projectId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List project members",
        description = "Returns all members of the project including their roles and permissions."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Members retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user does not have access"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            )
        }
    )
    public ResponseEntity<List<ProjectMemberResponse>> listMembers(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        List<ProjectMemberResponse> members = projectService.listMembers(
            companyId,
            projectId
        );
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Remove member from project",
        description = "Removes a user from the project. The project creator cannot be removed."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Member removed successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Cannot remove project creator"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            )
        }
    )
    public ResponseEntity<Void> removeMember(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "User ID to remove", required = true)
        @PathVariable UUID userId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        projectService.removeMember(companyId, projectId, userId);
        return ResponseEntity.noContent().build();
    }

    private void validateCompanyAccess(
        Authentication authentication,
        UUID companyId
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        if (userData.isMasterCompany()) {
            return;
        }

        if (!userData.companyId().equals(companyId)) {
            throw new IllegalStateException(
                "You can only access projects from your own company"
            );
        }
    }
}

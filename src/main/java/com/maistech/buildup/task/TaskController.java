package com.maistech.buildup.task;

import com.maistech.buildup.shared.security.JWTUserData;
import com.maistech.buildup.task.domain.TaskService;
import com.maistech.buildup.task.dto.*;
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
@RequestMapping("/companies/{companyId}/projects/{projectId}/tasks")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Tasks",
    description = "Task management for construction projects. Supports CRUD operations, progress tracking, dependencies, and status management."
)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create new task",
        description = "Creates a new task for the project. Tasks can be assigned to multiple users and have priorities, deadlines, and dependencies."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Task created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponse.class)
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
                description = "Project not found"
            )
        }
    )
    public ResponseEntity<TaskResponse> createTask(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Valid @RequestBody CreateTaskRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        TaskResponse task = taskService.createTask(
            companyId,
            projectId,
            userData.userId(),
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List project tasks",
        description = "Returns all tasks for the project including their status, priority, assigned users, and dependencies."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Tasks retrieved successfully"
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
    public ResponseEntity<List<TaskResponse>> listTasks(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        List<TaskResponse> tasks = taskService.listProjectTasks(
            companyId,
            projectId
        );
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List user's tasks",
        description = "Returns all tasks assigned to the authenticated user across all projects."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "User's tasks retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            )
        }
    )
    public ResponseEntity<List<TaskResponse>> listMyTasks(
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        List<TaskResponse> tasks = taskService.listUserTasks(userData.userId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "List overdue tasks",
        description = "Returns all tasks that are past their due date and not yet completed."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Overdue tasks retrieved successfully"
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
    public ResponseEntity<List<TaskResponse>> listOverdueTasks(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        List<TaskResponse> tasks = taskService.listOverdueTasks(
            companyId,
            projectId
        );
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get task by ID",
        description = "Returns detailed information about a specific task including assigned users, dependencies, and progress."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Task retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user does not have access"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<TaskResponse> getTask(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        TaskResponse task = taskService.getTaskById(
            companyId,
            projectId,
            taskId
        );
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update task",
        description = "Updates task information. All fields in the request are optional - only provided fields will be updated."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Task updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskResponse.class)
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
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<TaskResponse> updateTask(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        @Valid @RequestBody UpdateTaskRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        TaskResponse task = taskService.updateTask(
            companyId,
            projectId,
            taskId,
            request
        );
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Delete task",
        description = "Permanently deletes a task and all its dependencies. This action cannot be undone."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Task deleted successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<Void> deleteTask(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        taskService.deleteTask(companyId, projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Start task",
        description = "Changes task status to IN_PROGRESS. Used when work begins on the task."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Task started successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Task cannot be started (e.g., already completed)"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<TaskResponse> startTask(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        TaskResponse task = taskService.startTask(companyId, projectId, taskId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{taskId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Complete task",
        description = "Marks task as COMPLETED and sets progress to 100%. Used when task is finished."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Task completed successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Task cannot be completed (e.g., already cancelled)"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<TaskResponse> completeTask(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        TaskResponse task = taskService.completeTask(
            companyId,
            projectId,
            taskId
        );
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{taskId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Update task progress",
        description = "Updates the progress percentage of a task (0-100%). Used to track task completion."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Progress updated successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error - progress must be between 0 and 100"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<TaskResponse> updateProgress(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        @Valid @RequestBody UpdateProgressRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        TaskResponse task = taskService.updateProgress(
            companyId,
            projectId,
            taskId,
            request.progressPercentage()
        );

        return ResponseEntity.ok(task);
    }

    @PostMapping("/{taskId}/dependencies")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Add task dependency",
        description = "Creates a dependency relationship between tasks. Types: BLOCKS (this task blocks another), DEPENDS_ON (this task depends on another), RELATED (tasks are related)."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Dependency added successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error or circular dependency detected"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task not found"
            )
        }
    )
    public ResponseEntity<Void> addDependency(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        @Valid @RequestBody AddDependencyRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        taskService.addDependency(companyId, projectId, taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{taskId}/dependencies/{dependsOnTaskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Remove task dependency",
        description = "Removes a dependency relationship between two tasks."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Dependency removed successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Task or dependency not found"
            )
        }
    )
    public ResponseEntity<Void> removeDependency(
        @Parameter(description = "Company ID", required = true)
        @PathVariable UUID companyId,
        @Parameter(description = "Project ID", required = true)
        @PathVariable UUID projectId,
        @Parameter(description = "Task ID", required = true)
        @PathVariable UUID taskId,
        @Parameter(description = "Depends On Task ID", required = true)
        @PathVariable UUID dependsOnTaskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        taskService.removeDependency(
            companyId,
            projectId,
            taskId,
            dependsOnTaskId
        );
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
                "You can only access tasks from your own company"
            );
        }
    }
}

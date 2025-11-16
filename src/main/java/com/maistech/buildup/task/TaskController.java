package com.maistech.buildup.task;

import com.maistech.buildup.shared.config.JWTUserData;
import com.maistech.buildup.task.dto.*;
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
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> createTask(
        @PathVariable UUID companyId,
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
    public ResponseEntity<List<TaskResponse>> listTasks(
        @PathVariable UUID companyId,
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
    public ResponseEntity<List<TaskResponse>> listMyTasks(
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        List<TaskResponse> tasks = taskService.listUserTasks(userData.userId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TaskResponse>> listOverdueTasks(
        @PathVariable UUID companyId,
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
    public ResponseEntity<TaskResponse> getTask(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
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
    public ResponseEntity<TaskResponse> updateTask(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
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
    public ResponseEntity<Void> deleteTask(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        taskService.deleteTask(companyId, projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskResponse> startTask(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        TaskResponse task = taskService.startTask(companyId, projectId, taskId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{taskId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskResponse> completeTask(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
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
    public ResponseEntity<TaskResponse> updateProgress(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
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
    public ResponseEntity<Void> addDependency(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
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
    public ResponseEntity<Void> removeDependency(
        @PathVariable UUID companyId,
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
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

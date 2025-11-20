package com.maistech.buildup.task.domain;

import com.maistech.buildup.task.*;
import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.ProjectNotFoundException;
import com.maistech.buildup.project.domain.ProjectRepository;
import com.maistech.buildup.task.dto.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
        TaskRepository taskRepository,
        TaskDependencyRepository taskDependencyRepository,
        ProjectRepository projectRepository,
        UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(
        UUID companyId,
        UUID projectId,
        UUID createdById,
        CreateTaskRequest request
    ) {
        ProjectEntity project = projectRepository
            .findByIdAndCompanyId(projectId, companyId)
            .orElseThrow(() ->
                new ProjectNotFoundException(
                    "Project not found or does not belong to this company"
                )
            );

        UserEntity creator = userRepository
            .findById(createdById)
            .orElseThrow(() ->
                new IllegalArgumentException("Creator not found")
            );

        UserEntity assignedUser = null;
        if (request.assignedTo() != null) {
            assignedUser = userRepository
                .findById(request.assignedTo())
                .orElseThrow(() ->
                    new IllegalArgumentException("Assigned user not found")
                );
        }

        TaskEntity task = TaskEntity.builder()
            .project(project)
            .name(request.name())
            .description(request.description())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .durationDays(request.durationDays())
            .priority(
                request.priority() != null
                    ? request.priority()
                    : TaskPriority.MEDIUM
            )
            .progressPercentage(
                request.progressPercentage() != null
                    ? request.progressPercentage()
                    : 0
            )
            .createdBy(creator)
            .assignedTo(assignedUser)
            .companyId(companyId)
            .build();

        if (
            task.getStartDate() != null &&
            task.getDurationDays() != null &&
            task.getEndDate() == null
        ) {
            task.calculateEndDateFromDuration();
        } else if (
            task.getStartDate() != null &&
            task.getEndDate() != null &&
            task.getDurationDays() == null
        ) {
            task.calculateDurationFromDates();
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public TaskResponse updateTask(
        UUID companyId,
        UUID projectId,
        UUID taskId,
        UpdateTaskRequest request
    ) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );

        if (request.name() != null) {
            task.setName(request.name());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.startDate() != null) {
            task.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            task.setEndDate(request.endDate());
        }
        if (request.durationDays() != null) {
            task.setDurationDays(request.durationDays());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.progressPercentage() != null) {
            task.updateProgress(request.progressPercentage());
        }
        if (request.assignedTo() != null) {
            UserEntity assignedUser = userRepository
                .findById(request.assignedTo())
                .orElseThrow(() ->
                    new IllegalArgumentException("Assigned user not found")
                );
            task.setAssignedTo(assignedUser);
        }

        if (task.getStartDate() != null && task.getEndDate() != null) {
            task.calculateDurationFromDates();
        } else if (
            task.getStartDate() != null && task.getDurationDays() != null
        ) {
            task.calculateEndDateFromDuration();
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public void deleteTask(UUID companyId, UUID projectId, UUID taskId) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(
        UUID companyId,
        UUID projectId,
        UUID taskId
    ) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );
        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listProjectTasks(UUID companyId, UUID projectId) {
        projectRepository
            .findByIdAndCompanyId(projectId, companyId)
            .orElseThrow(() ->
                new ProjectNotFoundException("Project not found")
            );

        return taskRepository
            .findByProjectIdOrderByOrderIndexAsc(projectId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listUserTasks(UUID userId) {
        return taskRepository
            .findByAssignedToId(userId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listOverdueTasks(UUID companyId, UUID projectId) {
        projectRepository
            .findByIdAndCompanyId(projectId, companyId)
            .orElseThrow(() ->
                new ProjectNotFoundException("Project not found")
            );

        return taskRepository
            .findOverdueTasks(projectId, LocalDate.now())
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public TaskResponse startTask(UUID companyId, UUID projectId, UUID taskId) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );
        task.start();
        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public TaskResponse completeTask(
        UUID companyId,
        UUID projectId,
        UUID taskId
    ) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );
        task.complete();
        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public TaskResponse updateProgress(
        UUID companyId,
        UUID projectId,
        UUID taskId,
        Integer progress
    ) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );
        task.updateProgress(progress);
        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    public void addDependency(
        UUID companyId,
        UUID projectId,
        UUID taskId,
        AddDependencyRequest request
    ) {
        TaskEntity task = findTaskInProjectOrThrow(
            taskId,
            projectId,
            companyId
        );
        TaskEntity dependsOnTask = findTaskInProjectOrThrow(
            request.dependsOnTaskId(),
            projectId,
            companyId
        );

        if (
            taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(
                taskId,
                request.dependsOnTaskId()
            )
        ) {
            throw new IllegalArgumentException("Dependency already exists");
        }

        if (wouldCreateCircularDependency(taskId, request.dependsOnTaskId())) {
            throw new IllegalArgumentException(
                "Cannot add dependency: would create circular reference"
            );
        }

        TaskDependencyEntity dependency = new TaskDependencyEntity();
        dependency.setTask(task);
        dependency.setDependsOnTask(dependsOnTask);
        dependency.setDependencyType(request.dependencyType());

        taskDependencyRepository.save(dependency);
    }

    public void removeDependency(
        UUID companyId,
        UUID projectId,
        UUID taskId,
        UUID dependsOnTaskId
    ) {
        findTaskInProjectOrThrow(taskId, projectId, companyId);
        taskDependencyRepository.deleteByTaskIdAndDependsOnTaskId(
            taskId,
            dependsOnTaskId
        );
    }

    private boolean wouldCreateCircularDependency(
        UUID taskId,
        UUID dependsOnTaskId
    ) {
        return checkCircularDependency(dependsOnTaskId, taskId);
    }

    private boolean checkCircularDependency(
        UUID currentTaskId,
        UUID targetTaskId
    ) {
        if (currentTaskId.equals(targetTaskId)) {
            return true;
        }

        List<TaskDependencyEntity> dependencies =
            taskDependencyRepository.findByTaskId(currentTaskId);
        for (TaskDependencyEntity dep : dependencies) {
            if (
                checkCircularDependency(
                    dep.getDependsOnTask().getId(),
                    targetTaskId
                )
            ) {
                return true;
            }
        }

        return false;
    }

    private TaskEntity findTaskInProjectOrThrow(
        UUID taskId,
        UUID projectId,
        UUID companyId
    ) {
        TaskEntity task = taskRepository
            .findById(taskId)
            .orElseThrow(() ->
                new TaskNotFoundException("Task not found: " + taskId)
            );

        if (!task.getProject().getId().equals(projectId)) {
            throw new TaskNotFoundException(
                "Task does not belong to this project"
            );
        }

        if (!task.getProject().getCompanyId().equals(companyId)) {
            throw new IllegalStateException(
                "Task does not belong to your company"
            );
        }

        return task;
    }

    private TaskResponse mapToResponse(TaskEntity task) {
        AssignedUserDto assignedUser = null;
        if (task.getAssignedTo() != null) {
            assignedUser = new AssignedUserDto(
                task.getAssignedTo().getId(),
                task.getAssignedTo().getName(),
                task.getAssignedTo().getEmail()
            );
        }

        List<TaskDependencyDto> dependencies = task
            .getDependencies()
            .stream()
            .map(dep ->
                new TaskDependencyDto(
                    dep.getId(),
                    dep.getDependsOnTask().getId(),
                    dep.getDependsOnTask().getName(),
                    dep.getDependsOnTask().getStatus(),
                    dep.getDependencyType()
                )
            )
            .collect(Collectors.toList());

        return new TaskResponse(
            task.getId(),
            task.getProject().getId(),
            task.getProject().getName(),
            task.getName(),
            task.getDescription(),
            task.getStartDate(),
            task.getEndDate(),
            task.getDurationDays(),
            task.getStatus(),
            task.getPriority(),
            task.getProgressPercentage(),
            assignedUser,
            task.isOverdue(),
            task.getDaysUntilDueDate(),
            task.hasBlockingDependencies(),
            dependencies,
            task.getCreatedBy().getId(),
            task.getCreatedBy().getName(),
            task.getCreatedAt()
        );
    }
}

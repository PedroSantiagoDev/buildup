package com.maistech.buildup.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.ProjectNotFoundException;
import com.maistech.buildup.project.ProjectRepository;
import com.maistech.buildup.task.dto.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskDependencyRepository taskDependencyRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID companyId;
    private UUID projectId;
    private UUID userId;
    private UUID taskId;
    private CompanyEntity company;
    private ProjectEntity project;
    private UserEntity user;
    private UserEntity mockUser;
    private TaskEntity task;
    private CreateTaskRequest createRequest;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Test Company");

        project = new ProjectEntity();
        project.setId(projectId);
        project.setName("Test Project");
        project.setCompanyId(companyId);

        user = new UserEntity();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setCompany(company);

        mockUser = mock(UserEntity.class);
        lenient().when(mockUser.getId()).thenReturn(userId);
        lenient().when(mockUser.getName()).thenReturn("John Doe");
        lenient().when(mockUser.getEmail()).thenReturn("john@example.com");
        lenient().when(mockUser.getCompany()).thenReturn(company);

        task = new TaskEntity();
        task.setId(taskId);
        task.setProject(project);
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProgressPercentage(0);
        task.setCreatedBy(mockUser);

        createRequest = new CreateTaskRequest(
            "New Task",
            "Task description",
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            Integer.valueOf(7),
            TaskPriority.HIGH,
            userId,
            Integer.valueOf(0)
        );
    }

    @Test
    @DisplayName("createTask - should create task successfully")
    void shouldCreateTaskSuccessfully() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(
            companyId,
            projectId,
            userId,
            createRequest
        );

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(taskId);

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(
            TaskEntity.class
        );
        verify(taskRepository).save(captor.capture());

        TaskEntity savedTask = captor.getValue();
        assertThat(savedTask.getName()).isEqualTo("New Task");
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    @DisplayName("createTask - should throw exception when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            taskService.createTask(companyId, projectId, userId, createRequest)
        )
            .isInstanceOf(ProjectNotFoundException.class)
            .hasMessageContaining("Project not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTask - should throw exception when creator not found")
    void shouldThrowExceptionWhenCreatorNotFound() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            taskService.createTask(companyId, projectId, userId, createRequest)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Creator not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTask - should update task progress")
    void shouldUpdateTaskProgressSuccessfully() {
        UpdateTaskRequest updateRequest = new UpdateTaskRequest(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Integer.valueOf(50)
        );

        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));
        lenient()
            .when(userRepository.findById(userId))
            .thenReturn(Optional.of(mockUser));
        lenient()
            .when(taskRepository.save(any(TaskEntity.class)))
            .thenReturn(task);

        TaskResponse response = taskService.updateTask(
            companyId,
            projectId,
            taskId,
            updateRequest
        );

        assertThat(response).isNotNull();
        verify(taskRepository).save(task);
        assertThat(task.getProgressPercentage()).isEqualTo(50);
    }

    @Test
    @DisplayName("updateTask - should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFoundOnUpdate() {
        UpdateTaskRequest updateRequest = new UpdateTaskRequest(
            "Updated",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            taskService.updateTask(companyId, projectId, taskId, updateRequest)
        )
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTask - should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));

        taskService.deleteTask(companyId, projectId, taskId);

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("getTaskById - should return task successfully")
    void shouldGetTaskByIdSuccessfully() {
        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));

        TaskResponse response = taskService.getTaskById(
            companyId,
            projectId,
            taskId
        );

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(taskId);
        assertThat(response.name()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("listProjectTasks - should return all project tasks")
    void shouldListProjectTasksSuccessfully() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(
            taskRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
        ).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.listProjectTasks(
            companyId,
            projectId
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("listUserTasks - should return user's assigned tasks")
    void shouldListUserTasksSuccessfully() {
        when(taskRepository.findByAssignedToId(userId)).thenReturn(
            List.of(task)
        );

        List<TaskResponse> response = taskService.listUserTasks(userId);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("listOverdueTasks - should return overdue tasks")
    void shouldListOverdueTasksSuccessfully() {
        task.setEndDate(LocalDate.now().minusDays(1));
        task.setStatus(TaskStatus.IN_PROGRESS);

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(
            taskRepository.findOverdueTasks(eq(projectId), any(LocalDate.class))
        ).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.listOverdueTasks(
            companyId,
            projectId
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("startTask - should update task status to IN_PROGRESS")
    void shouldStartTaskSuccessfully() {
        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));
        lenient()
            .when(taskRepository.save(any(TaskEntity.class)))
            .thenReturn(task);

        TaskResponse response = taskService.startTask(
            companyId,
            projectId,
            taskId
        );

        assertThat(response).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("completeTask - should update task status to COMPLETED")
    void shouldCompleteTaskSuccessfully() {
        task.setStatus(TaskStatus.IN_PROGRESS);

        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));
        lenient()
            .when(taskRepository.save(any(TaskEntity.class)))
            .thenReturn(task);

        TaskResponse response = taskService.completeTask(
            companyId,
            projectId,
            taskId
        );

        assertThat(response).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getProgressPercentage()).isEqualTo(100);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("updateProgress - should update task progress")
    void shouldUpdateProgressSuccessfully() {
        UpdateProgressRequest progressRequest = new UpdateProgressRequest(
            Integer.valueOf(75)
        );

        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));
        lenient()
            .when(taskRepository.save(any(TaskEntity.class)))
            .thenReturn(task);

        TaskResponse response = taskService.updateProgress(
            companyId,
            projectId,
            taskId,
            progressRequest.progressPercentage()
        );

        assertThat(response).isNotNull();
        assertThat(task.getProgressPercentage()).isEqualTo(75);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("addDependency - should add dependency successfully")
    void shouldAddDependencySuccessfully() {
        UUID dependentTaskId = UUID.randomUUID();
        TaskEntity dependentTask = new TaskEntity();
        dependentTask.setId(dependentTaskId);
        dependentTask.setProject(project);
        dependentTask.setName("Dependent Task");

        AddDependencyRequest request = new AddDependencyRequest(
            dependentTaskId,
            DependencyType.FINISH_TO_START
        );

        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(taskRepository.findById(dependentTaskId))
            .thenReturn(Optional.of(dependentTask));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));
        lenient()
            .when(
                taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(
                    taskId,
                    dependentTaskId
                )
            )
            .thenReturn(false);

        taskService.addDependency(companyId, projectId, taskId, request);

        verify(taskDependencyRepository).save(any(TaskDependencyEntity.class));
    }

    @Test
    @DisplayName(
        "addDependency - should throw exception when circular dependency detected"
    )
    void shouldThrowExceptionOnCircularDependency() {
        UUID dependentTaskId = UUID.randomUUID();
        TaskEntity dependentTask = new TaskEntity();
        dependentTask.setId(dependentTaskId);
        dependentTask.setProject(project);

        AddDependencyRequest request = new AddDependencyRequest(
            taskId,
            DependencyType.FINISH_TO_START
        );

        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));

        assertThatThrownBy(() ->
            taskService.addDependency(companyId, projectId, taskId, request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("would create circular reference");

        verify(taskDependencyRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeDependency - should remove dependency successfully")
    void shouldRemoveDependencySuccessfully() {
        UUID dependencyId = UUID.randomUUID();

        lenient()
            .when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));
        lenient()
            .when(projectRepository.findByIdAndCompanyId(projectId, companyId))
            .thenReturn(Optional.of(project));

        taskService.removeDependency(
            companyId,
            projectId,
            taskId,
            dependencyId
        );

        verify(taskDependencyRepository).deleteByTaskIdAndDependsOnTaskId(
            taskId,
            dependencyId
        );
    }
}

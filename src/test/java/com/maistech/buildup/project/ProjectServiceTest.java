package com.maistech.buildup.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.project.dto.*;
import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID companyId;
    private UUID userId;
    private UUID projectId;
    private CompanyEntity company;
    private UserEntity user;
    private UserEntity mockUser;
    private ProjectEntity project;
    private CreateProjectRequest createRequest;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Test Company");

        user = new UserEntity();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setCompany(company);

        mockUser = mock(UserEntity.class);
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getName()).thenReturn("John Doe");
        when(mockUser.getEmail()).thenReturn("john@example.com");
        when(mockUser.getCompany()).thenReturn(company);

        project = new ProjectEntity();
        project.setId(projectId);
        project.setName("Test Project");
        project.setClientName("Test Client");
        project.setDescription("Test Description");
        project.setStartDate(LocalDate.now());
        project.setDueDate(LocalDate.now().plusDays(30));
        project.setContractValue(new BigDecimal("10000.00"));
        project.setDownPayment(new BigDecimal("3000.00"));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setCompanyId(companyId);
        project.setCreatedBy(mockUser);

        createRequest = new CreateProjectRequest(
            "New Project",
            "New Client",
            "Project description",
            LocalDate.now(),
            LocalDate.now().plusDays(60),
            new BigDecimal("20000.00"),
            new BigDecimal("5000.00"),
            "https://image.url/cover.jpg"
        );
    }

    @Test
    @DisplayName("createProject - should create project successfully")
    void shouldCreateProjectSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(
            project
        );

        ProjectResponse response = projectService.createProject(
            companyId,
            userId,
            createRequest
        );

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Test Project");

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(
            ProjectEntity.class
        );
        verify(projectRepository, times(2)).save(captor.capture());

        ProjectEntity savedProject = captor.getValue();
        assertThat(savedProject.getCompanyId()).isEqualTo(companyId);
        assertThat(savedProject.getCreatedBy()).isEqualTo(user);
    }

    @Test
    @DisplayName("createProject - should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            projectService.createProject(companyId, userId, createRequest)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "createProject - should throw exception when user not in company"
    )
    void shouldThrowExceptionWhenUserNotInCompany() {
        UUID differentCompanyId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
            projectService.createProject(
                differentCompanyId,
                userId,
                createRequest
            )
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not belong to this company");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProject - should update project successfully")
    void shouldUpdateProjectSuccessfully() {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest(
            "Updated Project",
            "Updated Client",
            "Updated description",
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(90),
            new BigDecimal("25000.00"),
            new BigDecimal("7000.00"),
            "https://new-image.url",
            ProjectStatus.COMPLETED
        );

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(
            project
        );

        ProjectResponse response = projectService.updateProject(
            companyId,
            projectId,
            updateRequest
        );

        assertThat(response).isNotNull();
        verify(projectRepository).save(project);
        assertThat(project.getName()).isEqualTo("Updated Project");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName(
        "updateProject - should throw exception when project not found"
    )
    void shouldThrowExceptionWhenProjectNotFoundOnUpdate() {
        UpdateProjectRequest updateRequest = new UpdateProjectRequest(
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

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            projectService.updateProject(companyId, projectId, updateRequest)
        )
            .isInstanceOf(ProjectNotFoundException.class)
            .hasMessageContaining("Project not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteProject - should delete project successfully")
    void shouldDeleteProjectSuccessfully() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));

        projectService.deleteProject(companyId, projectId);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("getProjectById - should return project successfully")
    void shouldGetProjectByIdSuccessfully() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(
            companyId,
            projectId
        );

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(projectId);
        assertThat(response.name()).isEqualTo("Test Project");
    }

    @Test
    @DisplayName("listProjects - should return paginated projects")
    void shouldListProjectsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProjectEntity> projectPage = new PageImpl<>(List.of(project));

        when(projectRepository.findByCompanyId(companyId, pageable)).thenReturn(
            projectPage
        );

        Page<ProjectResponse> response = projectService.listProjects(
            companyId,
            pageable
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("listUserProjects - should return user's projects")
    void shouldListUserProjectsSuccessfully() {
        when(
            projectRepository.findByUserIdAndCompanyId(userId, companyId)
        ).thenReturn(List.of(project));

        List<ProjectResponse> response = projectService.listUserProjects(
            companyId,
            userId
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("addMember - should add member successfully")
    void shouldAddMemberSuccessfully() {
        UUID newMemberId = UUID.randomUUID();
        UserEntity newMember = mock(UserEntity.class);
        when(newMember.getId()).thenReturn(newMemberId);
        when(newMember.getName()).thenReturn("Jane Doe");
        when(newMember.getEmail()).thenReturn("jane@example.com");
        when(newMember.getCompany()).thenReturn(company);

        AddMemberRequest request = new AddMemberRequest(
            newMemberId,
            "Developer",
            true
        );

        UUID memberEntityId = UUID.randomUUID();

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(userRepository.findById(newMemberId)).thenReturn(
            Optional.of(newMember)
        );
        when(
            projectMemberRepository.existsByProjectIdAndUserId(
                projectId,
                newMemberId
            )
        ).thenReturn(false);
        when(
            projectMemberRepository.save(any(ProjectMemberEntity.class))
        ).thenAnswer(invocation -> {
            ProjectMemberEntity saved = invocation.getArgument(0);
            saved.setId(memberEntityId);
            return saved;
        });

        ProjectMemberResponse response = projectService.addMember(
            companyId,
            projectId,
            request
        );

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(newMemberId);
        assertThat(response.role()).isEqualTo("Developer");
        assertThat(response.canEdit()).isTrue();

        verify(projectMemberRepository).save(any(ProjectMemberEntity.class));
    }

    @Test
    @DisplayName("addMember - should throw exception when user not found")
    void shouldThrowExceptionWhenAddingNonExistentUser() {
        UUID newMemberId = UUID.randomUUID();
        AddMemberRequest request = new AddMemberRequest(
            newMemberId,
            "Developer",
            true
        );

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(userRepository.findById(newMemberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            projectService.addMember(companyId, projectId, request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("addMember - should throw exception when user already member")
    void shouldThrowExceptionWhenUserAlreadyMember() {
        UUID existingMemberId = UUID.randomUUID();
        UserEntity existingMember = mock(UserEntity.class);
        when(existingMember.getId()).thenReturn(existingMemberId);
        when(existingMember.getCompany()).thenReturn(company);

        AddMemberRequest request = new AddMemberRequest(
            existingMemberId,
            "Developer",
            true
        );

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(userRepository.findById(existingMemberId)).thenReturn(
            Optional.of(existingMember)
        );
        when(
            projectMemberRepository.existsByProjectIdAndUserId(
                projectId,
                existingMemberId
            )
        ).thenReturn(true);

        assertThatThrownBy(() ->
            projectService.addMember(companyId, projectId, request)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already a member");

        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeMember - should remove member successfully")
    void shouldRemoveMemberSuccessfully() {
        UUID memberToRemoveId = UUID.randomUUID();

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));

        projectService.removeMember(companyId, projectId, memberToRemoveId);

        verify(projectMemberRepository).deleteByProjectIdAndUserId(
            projectId,
            memberToRemoveId
        );
    }

    @Test
    @DisplayName("removeMember - should throw exception when removing creator")
    void shouldThrowExceptionWhenRemovingCreator() {
        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));

        assertThatThrownBy(() ->
            projectService.removeMember(companyId, projectId, userId)
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot remove project creator");

        verify(projectMemberRepository, never()).deleteByProjectIdAndUserId(
            any(),
            any()
        );
    }

    @Test
    @DisplayName("listMembers - should return all project members")
    void shouldListMembersSuccessfully() {
        ProjectMemberEntity member = new ProjectMemberEntity();
        member.setId(UUID.randomUUID());
        member.setProject(project);
        member.setUser(user);
        member.setRole("Developer");
        member.setCanEdit(true);

        when(
            projectRepository.findByIdAndCompanyId(projectId, companyId)
        ).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectId(projectId)).thenReturn(
            List.of(member)
        );

        List<ProjectMemberResponse> response = projectService.listMembers(
            companyId,
            projectId
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).userId()).isEqualTo(userId);
        assertThat(response.get(0).role()).isEqualTo("Developer");
    }
}

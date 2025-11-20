package com.maistech.buildup.project.domain;

import com.maistech.buildup.project.*;
import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.project.dto.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public ProjectResponse createProject(
        UUID companyId,
        UUID currentUserId,
        CreateProjectRequest request
    ) {
        UserEntity creator = userRepository
            .findById(currentUserId)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found: " + currentUserId)
            );

        if (!creator.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException(
                "User does not belong to this company"
            );
        }

        ProjectEntity project = ProjectEntity.builder()
            .name(request.name())
            .clientName(request.clientName())
            .description(request.description())
            .startDate(request.startDate())
            .dueDate(request.dueDate())
            .contractValue(request.contractValue())
            .downPayment(request.downPayment())
            .coverImageUrl(request.coverImageUrl())
            .status(ProjectStatus.IN_PROGRESS)
            .companyId(companyId)
            .createdBy(creator)
            .build();

        project = projectRepository.save(project);

        project.addMember(creator, "Project Manager", true);
        project = projectRepository.save(project);

        return mapToResponse(project);
    }

    public ProjectResponse updateProject(
        UUID companyId,
        UUID projectId,
        UpdateProjectRequest request
    ) {
        ProjectEntity project = findProjectInCompanyOrThrow(
            projectId,
            companyId
        );

        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.clientName() != null) {
            project.setClientName(request.clientName());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.startDate() != null) {
            project.setStartDate(request.startDate());
        }
        if (request.dueDate() != null) {
            project.setDueDate(request.dueDate());
        }
        if (request.contractValue() != null) {
            project.setContractValue(request.contractValue());
        }
        if (request.downPayment() != null) {
            project.setDownPayment(request.downPayment());
        }
        if (request.coverImageUrl() != null) {
            project.setCoverImageUrl(request.coverImageUrl());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    public void deleteProject(UUID companyId, UUID projectId) {
        ProjectEntity project = findProjectInCompanyOrThrow(
            projectId,
            companyId
        );
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID companyId, UUID projectId) {
        ProjectEntity project = findProjectInCompanyOrThrow(
            projectId,
            companyId
        );
        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listProjects(
        UUID companyId,
        Pageable pageable
    ) {
        return projectRepository
            .findByCompanyId(companyId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listUserProjects(UUID companyId, UUID userId) {
        return projectRepository
            .findByUserIdAndCompanyId(userId, companyId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public ProjectMemberResponse addMember(
        UUID companyId,
        UUID projectId,
        AddMemberRequest request
    ) {
        ProjectEntity project = findProjectInCompanyOrThrow(
            projectId,
            companyId
        );

        UserEntity user = userRepository
            .findById(request.userId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "User not found: " + request.userId()
                )
            );

        if (!user.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException(
                "User does not belong to this company"
            );
        }

        if (
            projectMemberRepository.existsByProjectIdAndUserId(
                projectId,
                request.userId()
            )
        ) {
            throw new IllegalArgumentException(
                "User is already a member of this project"
            );
        }

        ProjectMemberEntity member = new ProjectMemberEntity();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.role());
        member.setCanEdit(request.canEdit());

        member = projectMemberRepository.save(member);

        return new ProjectMemberResponse(
            member.getId(),
            user.getId(),
            user.getName(),
            user.getEmail(),
            member.getRole(),
            member.getCanEdit(),
            member.getJoinedAt()
        );
    }

    public void removeMember(UUID companyId, UUID projectId, UUID userId) {
        ProjectEntity project = findProjectInCompanyOrThrow(
            projectId,
            companyId
        );

        if (project.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException(
                "Cannot remove project creator from members"
            );
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(
        UUID companyId,
        UUID projectId
    ) {
        findProjectInCompanyOrThrow(projectId, companyId);

        return projectMemberRepository
            .findByProjectId(projectId)
            .stream()
            .map(member ->
                new ProjectMemberResponse(
                    member.getId(),
                    member.getUser().getId(),
                    member.getUser().getName(),
                    member.getUser().getEmail(),
                    member.getRole(),
                    member.getCanEdit(),
                    member.getJoinedAt()
                )
            )
            .collect(Collectors.toList());
    }

    private ProjectEntity findProjectInCompanyOrThrow(
        UUID projectId,
        UUID companyId
    ) {
        return projectRepository
            .findByIdAndCompanyId(projectId, companyId)
            .orElseThrow(() ->
                new ProjectNotFoundException(
                    "Project not found or does not belong to this company: " +
                        projectId
                )
            );
    }

    private ProjectResponse mapToResponse(ProjectEntity project) {
        List<ProjectMemberResponse> members = project
            .getMembers()
            .stream()
            .map(member ->
                new ProjectMemberResponse(
                    member.getId(),
                    member.getUser().getId(),
                    member.getUser().getName(),
                    member.getUser().getEmail(),
                    member.getRole(),
                    member.getCanEdit(),
                    member.getJoinedAt()
                )
            )
            .collect(Collectors.toList());

        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getClientName(),
            project.getDescription(),
            project.getStartDate(),
            project.getDueDate(),
            project.getContractValue(),
            project.getDownPayment(),
            project.getRemainingPayment(),
            project.getCoverImageUrl(),
            project.getStatus(),
            project.isOverdue(),
            project.getDaysUntilDueDate(),
            project.getCreatedBy().getId(),
            project.getCreatedBy().getName(),
            project.getCreatedAt(),
            members
        );
    }
}

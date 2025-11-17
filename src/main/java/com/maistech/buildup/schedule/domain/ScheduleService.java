package com.maistech.buildup.schedule.domain;

import com.maistech.buildup.schedule.*;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.domain.ProjectRepository;
import com.maistech.buildup.schedule.dto.*;
import com.maistech.buildup.task.TaskEntity;
import com.maistech.buildup.task.domain.TaskRepository;
import com.maistech.buildup.task.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ScheduleService(
        ScheduleRepository scheduleRepository,
        MilestoneRepository milestoneRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public ScheduleResponse generateSchedule(UUID companyId, UUID projectId) {
        ProjectEntity project = projectRepository
            .findById(projectId)
            .orElseThrow(() ->
                new ScheduleNotFoundException("Project not found")
            );

        ScheduleEntity schedule = scheduleRepository
            .findByProjectId(projectId)
            .orElseGet(() -> {
                ScheduleEntity newSchedule = new ScheduleEntity();
                newSchedule.setProject(project);
                newSchedule.setCompanyId(companyId);
                return newSchedule;
            });

        calculateSchedule(schedule, projectId);

        schedule = scheduleRepository.save(schedule);

        return toScheduleResponse(schedule);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleByProjectId(
        UUID companyId,
        UUID projectId
    ) {
        ScheduleEntity schedule = scheduleRepository
            .findByProjectId(projectId)
            .orElseThrow(() ->
                new ScheduleNotFoundException("Schedule not found for project")
            );

        return toScheduleResponse(schedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(
        UUID companyId,
        UUID projectId,
        UpdateScheduleRequest request
    ) {
        ScheduleEntity schedule = scheduleRepository
            .findByProjectId(projectId)
            .orElseThrow(() ->
                new ScheduleNotFoundException("Schedule not found")
            );

        if (request.startDate() != null) {
            schedule.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            schedule.setEndDate(request.endDate());
        }
        if (request.actualStartDate() != null) {
            schedule.setActualStartDate(request.actualStartDate());
        }
        if (request.actualEndDate() != null) {
            schedule.setActualEndDate(request.actualEndDate());
        }
        if (request.status() != null) {
            schedule.setStatus(request.status());
        }
        if (request.notes() != null) {
            schedule.setNotes(request.notes());
        }

        schedule = scheduleRepository.save(schedule);
        return toScheduleResponse(schedule);
    }

    @Transactional
    public ScheduleResponse recalculateSchedule(
        UUID companyId,
        UUID projectId
    ) {
        return generateSchedule(companyId, projectId);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> listCompanySchedules(UUID companyId) {
        return scheduleRepository
            .findAllByCompanyId(companyId)
            .stream()
            .map(this::toScheduleResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> listDelayedSchedules(UUID companyId) {
        return scheduleRepository
            .findDelayedSchedules(companyId)
            .stream()
            .map(this::toScheduleResponse)
            .toList();
    }

    private void calculateSchedule(ScheduleEntity schedule, UUID projectId) {
        List<TaskEntity> tasks = taskRepository.findByProjectId(projectId);

        if (tasks.isEmpty()) {
            schedule.setStatus(ScheduleStatus.DRAFT);
            schedule.setTotalTasks(0);
            schedule.setCompletedTasks(0);
            schedule.setOverdueTasks(0);
            schedule.setCompletedPercentage(0);
            return;
        }

        LocalDate earliestStart = tasks
            .stream()
            .map(TaskEntity::getStartDate)
            .filter(date -> date != null)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());

        LocalDate latestEnd = tasks
            .stream()
            .map(TaskEntity::getEndDate)
            .filter(date -> date != null)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now().plusMonths(3));

        schedule.setStartDate(earliestStart);
        schedule.setEndDate(latestEnd);
        schedule.setTotalDurationDays(
            (int) ChronoUnit.DAYS.between(earliestStart, latestEnd)
        );

        int totalTasks = tasks.size();
        long completedTasks = tasks
            .stream()
            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
            .count();
        long overdueTasks = tasks
            .stream()
            .filter(
                t ->
                    t.getEndDate() != null &&
                    t.getEndDate().isBefore(LocalDate.now()) &&
                    t.getStatus() != TaskStatus.COMPLETED
            )
            .count();

        schedule.setTotalTasks(totalTasks);
        schedule.setCompletedTasks((int) completedTasks);
        schedule.setOverdueTasks((int) overdueTasks);

        int completedPercentage = totalTasks > 0
            ? (int) ((completedTasks * 100) / totalTasks)
            : 0;
        schedule.setCompletedPercentage(completedPercentage);

        boolean isOnTrack =
            overdueTasks == 0 &&
            (schedule.getEndDate() == null ||
                !schedule.getEndDate().isBefore(LocalDate.now()));
        schedule.setIsOnTrack(isOnTrack);

        if (overdueTasks > 0) {
            schedule.setStatus(ScheduleStatus.DELAYED);
        } else if (completedTasks == totalTasks) {
            schedule.setStatus(ScheduleStatus.COMPLETED);
        } else if (completedTasks > 0) {
            schedule.setStatus(ScheduleStatus.ACTIVE);
        } else {
            schedule.setStatus(ScheduleStatus.DRAFT);
        }

        schedule.setLastCalculatedAt(LocalDateTime.now());
    }

    private ScheduleResponse toScheduleResponse(ScheduleEntity schedule) {
        List<MilestoneResponse> milestones = milestoneRepository
            .findByProjectIdOrderByPlannedDateAsc(schedule.getProject().getId())
            .stream()
            .map(this::toMilestoneResponse)
            .toList();

        Integer daysRemaining = schedule.getEndDate() != null
            ? (int) ChronoUnit.DAYS.between(
                  LocalDate.now(),
                  schedule.getEndDate()
              )
            : null;

        Integer daysElapsed = schedule.getStartDate() != null
            ? (int) ChronoUnit.DAYS.between(
                  schedule.getStartDate(),
                  LocalDate.now()
              )
            : null;

        return new ScheduleResponse(
            schedule.getId(),
            schedule.getProject().getId(),
            schedule.getProject().getName(),
            schedule.getStartDate(),
            schedule.getEndDate(),
            schedule.getActualStartDate(),
            schedule.getActualEndDate(),
            schedule.getTotalDurationDays(),
            schedule.getCompletedPercentage(),
            schedule.getTotalTasks(),
            schedule.getCompletedTasks(),
            schedule.getOverdueTasks(),
            schedule.getCriticalPathDuration(),
            schedule.getStatus(),
            schedule.getIsOnTrack(),
            schedule.getNotes(),
            schedule.getLastCalculatedAt(),
            milestones,
            daysRemaining,
            daysElapsed,
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }

    private MilestoneResponse toMilestoneResponse(MilestoneEntity milestone) {
        boolean isOverdue =
            milestone.getPlannedDate() != null &&
            milestone.getPlannedDate().isBefore(LocalDate.now()) &&
            milestone.getStatus() != MilestoneStatus.COMPLETED;

        Integer daysUntilDue = milestone.getPlannedDate() != null
            ? (int) ChronoUnit.DAYS.between(
                  LocalDate.now(),
                  milestone.getPlannedDate()
              )
            : null;

        return new MilestoneResponse(
            milestone.getId(),
            milestone.getName(),
            milestone.getDescription(),
            milestone.getPlannedDate(),
            milestone.getActualDate(),
            milestone.getStatus(),
            milestone.getType(),
            milestone.getCompletionPercentage(),
            milestone.getProject().getId(),
            milestone.getProject().getName(),
            milestone.getOrderIndex(),
            isOverdue,
            daysUntilDue,
            milestone.getCreatedAt(),
            milestone.getUpdatedAt()
        );
    }

    @Transactional
    public MilestoneResponse createMilestone(
        UUID companyId,
        UUID projectId,
        CreateMilestoneRequest request
    ) {
        ProjectEntity project = projectRepository
            .findById(projectId)
            .orElseThrow(() ->
                new ScheduleNotFoundException("Project not found")
            );

        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setCompanyId(companyId);
        milestone.setProject(project);
        milestone.setName(request.name());
        milestone.setDescription(request.description());
        milestone.setPlannedDate(request.plannedDate());
        milestone.setType(
            request.type() != null ? request.type() : MilestoneType.GENERAL
        );
        milestone.setCompletionPercentage(
            request.completionPercentage() != null
                ? request.completionPercentage()
                : 0
        );
        milestone.setOrderIndex(request.orderIndex());
        milestone.setStatus(MilestoneStatus.PENDING);

        milestone = milestoneRepository.save(milestone);

        recalculateSchedule(companyId, projectId);

        return toMilestoneResponse(milestone);
    }

    @Transactional
    public MilestoneResponse updateMilestone(
        UUID companyId,
        UUID projectId,
        UUID milestoneId,
        UpdateMilestoneRequest request
    ) {
        MilestoneEntity milestone = milestoneRepository
            .findById(milestoneId)
            .orElseThrow(() ->
                new ScheduleNotFoundException("Milestone not found")
            );

        if (request.name() != null) milestone.setName(request.name());
        if (request.description() != null) milestone.setDescription(
            request.description()
        );
        if (request.plannedDate() != null) milestone.setPlannedDate(
            request.plannedDate()
        );
        if (request.actualDate() != null) milestone.setActualDate(
            request.actualDate()
        );
        if (request.status() != null) milestone.setStatus(request.status());
        if (request.type() != null) milestone.setType(request.type());
        if (
            request.completionPercentage() != null
        ) milestone.setCompletionPercentage(request.completionPercentage());
        if (request.orderIndex() != null) milestone.setOrderIndex(
            request.orderIndex()
        );

        milestone = milestoneRepository.save(milestone);

        recalculateSchedule(companyId, projectId);

        return toMilestoneResponse(milestone);
    }

    @Transactional
    public void deleteMilestone(
        UUID companyId,
        UUID projectId,
        UUID milestoneId
    ) {
        MilestoneEntity milestone = milestoneRepository
            .findById(milestoneId)
            .orElseThrow(() ->
                new ScheduleNotFoundException("Milestone not found")
            );

        milestoneRepository.delete(milestone);

        recalculateSchedule(companyId, projectId);
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> listProjectMilestones(
        UUID companyId,
        UUID projectId
    ) {
        return milestoneRepository
            .findByProjectIdOrderByPlannedDateAsc(projectId)
            .stream()
            .map(this::toMilestoneResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> listOverdueMilestones(UUID companyId) {
        return milestoneRepository
            .findOverdueMilestones(companyId, LocalDate.now())
            .stream()
            .map(this::toMilestoneResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> listUpcomingMilestones(
        UUID companyId,
        Integer days
    ) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days != null ? days : 30);

        return milestoneRepository
            .findUpcomingMilestones(companyId, startDate, endDate)
            .stream()
            .map(this::toMilestoneResponse)
            .toList();
    }
}

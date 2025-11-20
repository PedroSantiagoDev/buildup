package com.maistech.buildup.schedule.domain;

import com.maistech.buildup.schedule.PhaseEntity;
import com.maistech.buildup.schedule.ScheduleEntity;
import com.maistech.buildup.schedule.dto.CreatePhaseRequest;
import com.maistech.buildup.schedule.dto.PhaseResponse;
import com.maistech.buildup.schedule.dto.UpdatePhaseRequest;
import com.maistech.buildup.task.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PhaseService {

    private final PhaseRepository phaseRepository;
    private final ScheduleRepository scheduleRepository;

    public PhaseService(
        PhaseRepository phaseRepository,
        ScheduleRepository scheduleRepository
    ) {
        this.phaseRepository = phaseRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public PhaseResponse createPhase(
        UUID companyId,
        UUID scheduleId,
        CreatePhaseRequest request
    ) {
        ScheduleEntity schedule = scheduleRepository
            .findByIdAndCompanyId(scheduleId, companyId)
            .orElseThrow(() ->
                new IllegalArgumentException("Schedule not found: " + scheduleId)
            );

        PhaseEntity phase = PhaseEntity.builder()
            .name(request.name())
            .description(request.description())
            .schedule(schedule)
            .startDate(request.startDate())
            .endDate(request.endDate())
            .orderIndex(
                request.orderIndex() != null
                    ? request.orderIndex()
                    : getNextOrderIndex(scheduleId)
            )
            .notes(request.notes())
            .companyId(companyId)
            .build();

        PhaseEntity saved = phaseRepository.save(phase);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PhaseResponse getPhaseById(UUID companyId, UUID scheduleId, UUID phaseId) {
        PhaseEntity phase = phaseRepository
            .findByIdAndCompanyId(phaseId, companyId)
            .orElseThrow(() ->
                new IllegalArgumentException("Phase not found: " + phaseId)
            );

        if (!phase.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException(
                "Phase does not belong to schedule: " + scheduleId
            );
        }

        return mapToResponse(phase);
    }

    @Transactional(readOnly = true)
    public List<PhaseResponse> listPhasesBySchedule(
        UUID companyId,
        UUID scheduleId
    ) {
        ScheduleEntity schedule = scheduleRepository
            .findByIdAndCompanyId(scheduleId, companyId)
            .orElseThrow(() ->
                new IllegalArgumentException("Schedule not found: " + scheduleId)
            );

        return phaseRepository
            .findByScheduleIdAndCompanyId(scheduleId, companyId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public PhaseResponse updatePhase(
        UUID companyId,
        UUID scheduleId,
        UUID phaseId,
        UpdatePhaseRequest request
    ) {
        PhaseEntity phase = phaseRepository
            .findByIdAndCompanyId(phaseId, companyId)
            .orElseThrow(() ->
                new IllegalArgumentException("Phase not found: " + phaseId)
            );

        if (!phase.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException(
                "Phase does not belong to schedule: " + scheduleId
            );
        }

        if (request.name() != null) phase.setName(request.name());
        if (request.description() != null) phase.setDescription(
            request.description()
        );
        if (request.startDate() != null) phase.setStartDate(request.startDate());
        if (request.endDate() != null) phase.setEndDate(request.endDate());
        if (request.actualStartDate() != null) phase.setActualStartDate(
            request.actualStartDate()
        );
        if (request.actualEndDate() != null) phase.setActualEndDate(
            request.actualEndDate()
        );
        if (request.status() != null) phase.setStatus(request.status());
        if (request.orderIndex() != null) phase.setOrderIndex(
            request.orderIndex()
        );
        if (request.completionPercentage() != null) phase.updateProgress(
            request.completionPercentage()
        );
        if (request.notes() != null) phase.setNotes(request.notes());

        PhaseEntity updated = phaseRepository.save(phase);
        return mapToResponse(updated);
    }

    public void deletePhase(UUID companyId, UUID scheduleId, UUID phaseId) {
        PhaseEntity phase = phaseRepository
            .findByIdAndCompanyId(phaseId, companyId)
            .orElseThrow(() ->
                new IllegalArgumentException("Phase not found: " + phaseId)
            );

        if (!phase.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException(
                "Phase does not belong to schedule: " + scheduleId
            );
        }

        phaseRepository.delete(phase);
    }

    private int getNextOrderIndex(UUID scheduleId) {
        long count = phaseRepository.countByScheduleId(scheduleId);
        return (int) count;
    }

    private PhaseResponse mapToResponse(PhaseEntity phase) {
        int totalTasks = phase.getTasks().size();
        int completedTasks = (int) phase
            .getTasks()
            .stream()
            .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
            .count();

        return new PhaseResponse(
            phase.getId(),
            phase.getName(),
            phase.getDescription(),
            phase.getSchedule().getId(),
            phase.getStartDate(),
            phase.getEndDate(),
            phase.getActualStartDate(),
            phase.getActualEndDate(),
            phase.getStatus(),
            phase.getOrderIndex(),
            phase.getCompletionPercentage(),
            phase.getDurationDays(),
            totalTasks,
            completedTasks,
            phase.isOverdue(),
            phase.getNotes(),
            phase.getCreatedAt(),
            phase.getUpdatedAt()
        );
    }
}

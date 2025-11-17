package com.maistech.buildup.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<MilestoneEntity, UUID> {

    @Query("SELECT m FROM MilestoneEntity m WHERE m.project.id = :projectId ORDER BY m.plannedDate ASC")
    List<MilestoneEntity> findByProjectIdOrderByPlannedDateAsc(@Param("projectId") UUID projectId);

    @Query("SELECT m FROM MilestoneEntity m WHERE m.project.id = :projectId AND m.status = :status ORDER BY m.plannedDate ASC")
    List<MilestoneEntity> findByProjectIdAndStatus(
        @Param("projectId") UUID projectId,
        @Param("status") MilestoneStatus status
    );

    @Query("SELECT m FROM MilestoneEntity m WHERE m.project.id = :projectId AND m.type = :type ORDER BY m.plannedDate ASC")
    List<MilestoneEntity> findByProjectIdAndType(
        @Param("projectId") UUID projectId,
        @Param("type") MilestoneType type
    );

    @Query("SELECT m FROM MilestoneEntity m WHERE m.companyId = :companyId AND m.plannedDate < :date AND m.status != 'COMPLETED' ORDER BY m.plannedDate ASC")
    List<MilestoneEntity> findOverdueMilestones(
        @Param("companyId") UUID companyId,
        @Param("date") LocalDate date
    );

    @Query("SELECT m FROM MilestoneEntity m WHERE m.companyId = :companyId AND m.plannedDate BETWEEN :startDate AND :endDate ORDER BY m.plannedDate ASC")
    List<MilestoneEntity> findUpcomingMilestones(
        @Param("companyId") UUID companyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

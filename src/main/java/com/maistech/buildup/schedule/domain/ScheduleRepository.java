package com.maistech.buildup.schedule.domain;

import com.maistech.buildup.schedule.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {

    Optional<ScheduleEntity> findByProjectId(UUID projectId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.id = :scheduleId AND s.companyId = :companyId")
    Optional<ScheduleEntity> findByIdAndCompanyId(
        @Param("scheduleId") UUID scheduleId,
        @Param("companyId") UUID companyId
    );

    @Query("SELECT s FROM ScheduleEntity s WHERE s.companyId = :companyId")
    List<ScheduleEntity> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.companyId = :companyId AND s.status = :status")
    List<ScheduleEntity> findByCompanyIdAndStatus(
        @Param("companyId") UUID companyId,
        @Param("status") ScheduleStatus status
    );

    @Query("SELECT s FROM ScheduleEntity s WHERE s.companyId = :companyId AND s.isOnTrack = false")
    List<ScheduleEntity> findDelayedSchedules(@Param("companyId") UUID companyId);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.companyId = :companyId AND s.endDate < :date AND s.status != 'COMPLETED'")
    List<ScheduleEntity> findOverdueSchedules(
        @Param("companyId") UUID companyId,
        @Param("date") LocalDate date
    );
}

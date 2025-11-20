package com.maistech.buildup.schedule.domain;

import com.maistech.buildup.schedule.PhaseEntity;
import com.maistech.buildup.schedule.PhaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhaseRepository extends JpaRepository<PhaseEntity, UUID> {

    @Query("SELECT p FROM PhaseEntity p WHERE p.schedule.id = :scheduleId ORDER BY p.orderIndex ASC")
    List<PhaseEntity> findByScheduleIdOrderByOrderIndex(@Param("scheduleId") UUID scheduleId);

    @Query("SELECT p FROM PhaseEntity p WHERE p.schedule.id = :scheduleId AND p.companyId = :companyId ORDER BY p.orderIndex ASC")
    List<PhaseEntity> findByScheduleIdAndCompanyId(
        @Param("scheduleId") UUID scheduleId,
        @Param("companyId") UUID companyId
    );

    @Query("SELECT p FROM PhaseEntity p WHERE p.id = :phaseId AND p.companyId = :companyId")
    Optional<PhaseEntity> findByIdAndCompanyId(
        @Param("phaseId") UUID phaseId,
        @Param("companyId") UUID companyId
    );

    @Query("SELECT p FROM PhaseEntity p WHERE p.companyId = :companyId AND p.status = :status")
    List<PhaseEntity> findByCompanyIdAndStatus(
        @Param("companyId") UUID companyId,
        @Param("status") PhaseStatus status
    );

    @Query("SELECT COUNT(p) FROM PhaseEntity p WHERE p.schedule.id = :scheduleId")
    long countByScheduleId(@Param("scheduleId") UUID scheduleId);
}

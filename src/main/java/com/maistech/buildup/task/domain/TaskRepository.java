package com.maistech.buildup.task.domain;

import com.maistech.buildup.task.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByProjectId(UUID projectId);
    
    List<TaskEntity> findByProjectIdOrderByOrderIndexAsc(UUID projectId);

    List<TaskEntity> findByProjectIdAndStatus(
        UUID projectId,
        TaskStatus status
    );

    List<TaskEntity> findByAssignedToId(UUID userId);

    @Query(
        "SELECT t FROM TaskEntity t WHERE t.project.id = :projectId AND t.assignedTo.id = :userId"
    )
    List<TaskEntity> findByProjectIdAndAssignedToId(
        @Param("projectId") UUID projectId,
        @Param("userId") UUID userId
    );

    @Query(
        "SELECT t FROM TaskEntity t WHERE t.project.id = :projectId AND t.endDate < :date AND t.status NOT IN ('COMPLETED', 'CANCELLED')"
    )
    List<TaskEntity> findOverdueTasks(
        @Param("projectId") UUID projectId,
        @Param("date") LocalDate date
    );

    @Query(
        "SELECT t FROM TaskEntity t WHERE t.project.id = :projectId AND t.priority IN ('HIGH', 'URGENT')"
    )
    List<TaskEntity> findHighPriorityTasks(@Param("projectId") UUID projectId);
}

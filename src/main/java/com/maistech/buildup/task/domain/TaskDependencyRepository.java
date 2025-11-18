package com.maistech.buildup.task.domain;

import com.maistech.buildup.task.TaskDependencyEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDependencyRepository
    extends JpaRepository<TaskDependencyEntity, UUID> {
    List<TaskDependencyEntity> findByTaskId(UUID taskId);

    List<TaskDependencyEntity> findByDependsOnTaskId(UUID taskId);

    void deleteByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);

    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);
}

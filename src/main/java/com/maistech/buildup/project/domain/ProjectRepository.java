package com.maistech.buildup.project.domain;

import com.maistech.buildup.project.*;
import com.maistech.buildup.project.ProjectStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    Page<ProjectEntity> findByCompanyId(UUID companyId, Pageable pageable);

    List<ProjectEntity> findByCompanyIdAndStatus(
        UUID companyId,
        ProjectStatus status
    );

    @Query(
        "SELECT p FROM ProjectEntity p WHERE p.companyId = :companyId AND p.id = :projectId"
    )
    Optional<ProjectEntity> findByIdAndCompanyId(
        @Param("projectId") UUID projectId,
        @Param("companyId") UUID companyId
    );

    @Query(
        """
        SELECT p FROM ProjectEntity p
        JOIN p.members m
        WHERE m.user.id = :userId AND p.companyId = :companyId
        """
    )
    List<ProjectEntity> findByUserIdAndCompanyId(
        @Param("userId") UUID userId,
        @Param("companyId") UUID companyId
    );

    boolean existsByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM ProjectEntity p JOIN p.members m " +
            "WHERE p.id = :projectId AND m.user.id = :userId")
    boolean isUserMember(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}

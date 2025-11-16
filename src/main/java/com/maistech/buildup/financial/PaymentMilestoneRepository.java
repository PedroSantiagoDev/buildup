package com.maistech.buildup.financial;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMilestoneRepository
    extends JpaRepository<PaymentMilestoneEntity, UUID> {
    List<PaymentMilestoneEntity> findByProjectIdOrderByMilestoneNumberAsc(
        UUID projectId
    );

    List<PaymentMilestoneEntity> findByProjectIdAndStatus(
        UUID projectId,
        MilestoneStatus status
    );
}

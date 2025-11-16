package com.maistech.buildup.financial;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseInstallmentRepository
    extends JpaRepository<ExpenseInstallmentEntity, UUID> {
    List<ExpenseInstallmentEntity> findByExpenseIdOrderByInstallmentNumberAsc(
        UUID expenseId
    );

    List<ExpenseInstallmentEntity> findByExpenseIdAndStatus(
        UUID expenseId,
        ExpenseStatus status
    );
}

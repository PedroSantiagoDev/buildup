package com.maistech.buildup.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {
    List<ExpenseEntity> findByProjectIdOrderByDueDateDesc(UUID projectId);

    List<ExpenseEntity> findByProjectIdAndStatus(
        UUID projectId,
        ExpenseStatus status
    );

    List<ExpenseEntity> findByProjectIdAndCategoryId(
        UUID projectId,
        UUID categoryId
    );

    @Query(
        "SELECT e FROM ExpenseEntity e WHERE e.project.id = :projectId AND e.dueDate < :date AND e.status = 'PENDING'"
    )
    List<ExpenseEntity> findOverdueExpenses(
        @Param("projectId") UUID projectId,
        @Param("date") LocalDate date
    );

    @Query(
        "SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.project.id = :projectId"
    )
    BigDecimal getTotalExpensesByProject(@Param("projectId") UUID projectId);

    @Query(
        "SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.project.id = :projectId AND e.status = 'PAID'"
    )
    BigDecimal getTotalPaidByProject(@Param("projectId") UUID projectId);

    @Query(
        "SELECT e.category.name, SUM(e.amount) FROM ExpenseEntity e WHERE e.project.id = :projectId GROUP BY e.category.name"
    )
    List<Object[]> getExpensesByCategory(@Param("projectId") UUID projectId);

    @Query(
        "SELECT e FROM ExpenseEntity e WHERE e.project.id = :projectId AND e.dueDate BETWEEN :startDate AND :endDate"
    )
    List<ExpenseEntity> findByProjectAndDateRange(
        @Param("projectId") UUID projectId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

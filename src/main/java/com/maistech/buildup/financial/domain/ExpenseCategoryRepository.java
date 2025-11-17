package com.maistech.buildup.financial.domain;

import com.maistech.buildup.financial.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseCategoryRepository
    extends JpaRepository<ExpenseCategoryEntity, UUID> {
    List<ExpenseCategoryEntity> findByIsActiveTrue();

    Optional<ExpenseCategoryEntity> findByName(String name);
}

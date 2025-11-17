package com.maistech.buildup.company.domain;

import com.maistech.buildup.company.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {
    Optional<CompanyEntity> findByDocument(String document);

    boolean existsByDocument(String document);

    @Query("SELECT c FROM CompanyEntity c WHERE c.isMaster = true")
    Optional<CompanyEntity> findMasterCompany();

    @Query(
        "SELECT c FROM CompanyEntity c WHERE c.masterCompany.id = :masterCompanyId"
    )
    List<CompanyEntity> findAllClientCompanies(
        @Param("masterCompanyId") UUID masterCompanyId
    );
}

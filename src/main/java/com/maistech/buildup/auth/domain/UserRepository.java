package com.maistech.buildup.auth.domain;

import com.maistech.buildup.auth.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    @Query(
        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.company LEFT JOIN FETCH u.roles WHERE u.email = :email"
    )
    Optional<UserEntity> findByEmailWithCompanyAndRoles(
        @Param("email") String email
    );

    boolean existsByEmail(String email);

    @Query(
        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.company LEFT JOIN FETCH u.roles WHERE u.id = :id"
    )
    Optional<UserEntity> findByIdWithCompanyAndRoles(@Param("id") UUID id);

    @Query("SELECT u FROM UserEntity u WHERE u.company.id = :companyId")
    List<UserEntity> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query(
        "SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.company.id = :companyId AND u.id = :userId"
    )
    Optional<UserEntity> findByIdAndCompanyId(
        @Param("userId") UUID userId,
        @Param("companyId") UUID companyId
    );
}

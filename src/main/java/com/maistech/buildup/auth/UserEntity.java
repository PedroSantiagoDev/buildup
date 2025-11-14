package com.maistech.buildup.auth;

import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.role.RoleEntity;
import com.maistech.buildup.role.RoleEnum;
import com.maistech.buildup.shared.tenant.TenantAware;
import com.maistech.buildup.shared.tenant.TenantListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@EntityListeners(TenantListener.class)
@Getter
public class UserEntity implements UserDetails, TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @Email
    private String email;

    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyEntity company;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    public void assignRole(RoleEntity role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.roles.add(role);
    }

    public void assignRoles(Set<RoleEntity> rolesToAssign) {
        if (rolesToAssign == null || rolesToAssign.isEmpty()) {
            return;
        }
        rolesToAssign.forEach(this::assignRole);
    }

    public void clearRoles() {
        this.roles.clear();
    }

    public boolean hasRole(RoleEnum role) {
        return roles.stream().anyMatch(r -> r.getName().equals(role.name()));
    }

    public boolean isSuperAdmin() {
        return hasRole(RoleEnum.SUPER_ADMIN);
    }

    public boolean isAdmin() {
        return hasRole(RoleEnum.ADMIN);
    }

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }

    @Override
    public UUID getCompanyId() {
        return company != null ? company.getId() : null;
    }

    @Override
    public void setCompanyId(UUID companyId) {
        if (
            companyId != null &&
            (company == null || !companyId.equals(company.getId()))
        ) {
            CompanyEntity newCompany = new CompanyEntity();
            newCompany.setId(companyId);
            this.company = newCompany;
        }
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles
            .stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

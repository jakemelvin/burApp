package com.packt.blurApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.packt.blurApp.config.security.RoleNames;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String userName;

    @Email(message = "Email should be valid")
    @Column(unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    // Support for multiple roles per user
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    // Legacy single role field - kept for backward compatibility during migration
    // This will be deprecated and removed in future versions
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = true)
    private Role role;

    // NOTE: nullable=true to allow migration on existing DB rows.
    // We default to true in getters and in DataInitializer backfill.
    @Column(nullable = true)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = true, name = "account_non_expired")
    @Builder.Default
    private Boolean accountNonExpired = true;

    @Column(nullable = true, name = "account_non_locked")
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Column(nullable = true, name = "credentials_non_expired")
    @Builder.Default
    private Boolean credentialsNonExpired = true;

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Score> scores = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "user_race_participation", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "race_id")
    )
    @Builder.Default
    private Set<Race> races = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Add authorities from multiple roles (new system)
        if (roles != null && !roles.isEmpty()) {
            for (Role r : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName()));
                
                if (r.getPermissions() != null) {
                    authorities.addAll(
                        r.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.name()))
                            .collect(Collectors.toSet())
                    );
                }
            }
        }
        
        // Fallback to legacy single role for backward compatibility
        if (authorities.isEmpty() && role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            
            if (role.getPermissions() != null) {
                authorities.addAll(
                    role.getPermissions().stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.name()))
                        .collect(Collectors.toSet())
                );
            }
        }
        
        return authorities;
    }
    
    // Helper methods for multiple roles
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    public boolean hasRole(String roleName) {
        if (roleName == null) return false;
        String normalized = roleName.trim().toUpperCase();
        return roles.stream().anyMatch(r -> normalized.equalsIgnoreCase(r.getName())) ||
               (role != null && normalized.equalsIgnoreCase(role.getName()));
    }

    public boolean isGreatAdmin() {
        return hasRole(RoleNames.GREAT_ADMIN);
    }
    
    public Set<Role> getAllRoles() {
        Set<Role> allRoles = new HashSet<>(roles);
        if (role != null && allRoles.isEmpty()) {
            allRoles.add(role);
        }
        return allRoles;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired == null ? true : accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked == null ? true : accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired == null ? true : credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled == null ? true : enabled;
    }

    // Helper methods
    public void addRace(Race race) {
        this.races.add(race);
        race.getParticipants().add(this);
    }

    public void removeRace(Race race) {
        this.races.remove(race);
        race.getParticipants().remove(this);
    }
}

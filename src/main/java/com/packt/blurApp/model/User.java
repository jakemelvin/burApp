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
        
        // Add role as authority
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));
            
            // Add permissions as authorities
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

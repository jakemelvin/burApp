package com.packt.blurApp.model;

import com.packt.blurApp.model.enums.PermissionType;
import com.packt.blurApp.config.security.RoleNames;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"permissions"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 255)
    private String description;
    
    @ElementCollection(targetClass = PermissionType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    @Builder.Default
    private Set<PermissionType> permissions = new HashSet<>();
    
    // Helper method to check if role has a specific permission
    public boolean hasPermission(PermissionType permission) {
        return permissions.contains(PermissionType.ALL_PERMISSIONS) || 
               permissions.contains(permission);
    }
    
    // Predefined roles with their permissions
    public static Role createGreatAdminRole() {
        return Role.builder()
                .name(RoleNames.GREAT_ADMIN)
                .description("Great Administrator with all permissions")
                .permissions(new HashSet<>(Set.of(
                    PermissionType.ALL_PERMISSIONS,
                    PermissionType.CREATE_USER,
                    PermissionType.UPDATE_USER,
                    PermissionType.DELETE_USER,
                    PermissionType.VIEW_ALL_USERS,
                    PermissionType.ASSIGN_ROLES,
                    PermissionType.CREATE_PARTY,
                    PermissionType.JOIN_PARTY,
                    PermissionType.MANAGE_PARTY,
                    PermissionType.DELETE_PARTY,
                    PermissionType.VIEW_PARTY,
                    PermissionType.CREATE_RACE,
                    PermissionType.START_RACE,
                    PermissionType.JOIN_RACE,
                    PermissionType.LEAVE_RACE,
                    PermissionType.DELETE_RACE,
                    PermissionType.VIEW_RACE,
                    PermissionType.SUBMIT_SCORE,
                    PermissionType.VIEW_SCORE,
                    PermissionType.EDIT_SCORE,
                    PermissionType.VIEW_CARS,
                    PermissionType.VIEW_MAPS,
                    PermissionType.VIEW_STATISTICS,
                    PermissionType.VIEW_HISTORY,
                    PermissionType.UPDATE_OWN_PROFILE,
                    PermissionType.VIEW_OWN_PROFILE
                )))
                .build();
    }
    
    @Deprecated
    public static Role createPartyManagerRole() {
        return Role.builder()
                .name("PARTY_MANAGER")
                .description("Party Manager can create and manage parties and races")
                .permissions(new HashSet<>(Set.of(
                    PermissionType.CREATE_PARTY,
                    PermissionType.JOIN_PARTY,
                    PermissionType.MANAGE_PARTY,
                    PermissionType.VIEW_PARTY,
                    PermissionType.CREATE_RACE,
                    PermissionType.START_RACE,
                    PermissionType.JOIN_RACE,
                    PermissionType.LEAVE_RACE,
                    PermissionType.VIEW_RACE,
                    PermissionType.DELETE_RACE,
                    PermissionType.SUBMIT_SCORE,
                    PermissionType.VIEW_SCORE,
                    PermissionType.VIEW_CARS,
                    PermissionType.VIEW_MAPS,
                    PermissionType.VIEW_STATISTICS,
                    PermissionType.VIEW_HISTORY,
                    PermissionType.UPDATE_OWN_PROFILE,
                    PermissionType.VIEW_OWN_PROFILE
                )))
                .build();
    }
    
    public static Role createRacerRole() {
        // RACER is the main non-admin role.
        // It includes all permissions that the old PARTY_MANAGER role used to have.
        return Role.builder()
                .name("RACER")
                .description("Racer can create/manage parties and participate in races")
                .permissions(new HashSet<>(Set.of(
                    // Party
                    PermissionType.CREATE_PARTY,
                    PermissionType.JOIN_PARTY,
                    PermissionType.MANAGE_PARTY,
                    PermissionType.VIEW_PARTY,

                    // Race
                    PermissionType.CREATE_RACE,
                    PermissionType.START_RACE,
                    PermissionType.JOIN_RACE,
                    PermissionType.LEAVE_RACE,
                    PermissionType.VIEW_RACE,
                    PermissionType.DELETE_RACE,

                    // Scores
                    PermissionType.SUBMIT_SCORE,
                    PermissionType.VIEW_SCORE,
                    PermissionType.EDIT_SCORE,

                    // Catalog + stats
                    PermissionType.VIEW_CARS,
                    PermissionType.VIEW_MAPS,
                    PermissionType.VIEW_STATISTICS,
                    PermissionType.VIEW_HISTORY,

                    // Profile
                    PermissionType.UPDATE_OWN_PROFILE,
                    PermissionType.VIEW_OWN_PROFILE
                )))
                .build();
    }
}

package com.packt.blurApp.model;

import com.packt.blurApp.model.enums.PartyRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents the relationship between a User and a Party with a specific role.
 * This enables the Creator + Co-Hosts management system.
 */
@Entity
@Table(name = "party_member", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"party_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PartyRole role = PartyRole.PARTICIPANT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isHost() {
        return this.role == PartyRole.HOST;
    }

    public boolean isCoHost() {
        return this.role == PartyRole.CO_HOST;
    }

    public boolean isParticipant() {
        return this.role == PartyRole.PARTICIPANT;
    }

    public boolean canManageParty() {
        return this.role == PartyRole.HOST || this.role == PartyRole.CO_HOST;
    }

    public boolean canAssignCoHosts() {
        return this.role == PartyRole.HOST;
    }

    public boolean canDeleteParty() {
        return this.role == PartyRole.HOST;
    }
}

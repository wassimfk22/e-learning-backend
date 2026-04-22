package com.school.elearning.model;
import com.school.elearning.model.enums.Role;
import com.school.elearning.model.enums.RoleLive;

import jakarta.persistence.*;
import lombok.*;
 
@Entity @Table(
    name = "live_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"utilisateur_id", "live_session_id"})
)
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @Builder
public class LiveParticipant {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleLive role;
 
    @Builder.Default
    private boolean micOn = false;
 
    @Builder.Default
    private boolean cameraOn = false;
 
    @Builder.Default
    private boolean isMuted = false;
 
    @Builder.Default
    private boolean handRaised = false;
    
}
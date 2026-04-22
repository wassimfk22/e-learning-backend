package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
 
@Entity @Table(
    name = "live_permissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "live_session_id"})
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LivePermission {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;
 
    @Builder.Default
    private boolean canSpeak = false;
 
    @Builder.Default
    private boolean canShareScreen = false;
 
    @Builder.Default
    private boolean isBlocked = false;
    
}
package com.school.elearning.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.school.elearning.model.enums.RoleLive;
 
@Entity @Table(name = "live_messages")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter 
public class LiveMessage {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
 
    @Column(nullable = false)
    private LocalDateTime timestamp;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Utilisateur sender;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;
 
    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
    
}
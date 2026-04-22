package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.school.elearning.model.enums.SessionStatus;
 
@Entity @Table(name = "live_sessions")
@NoArgsConstructor @AllArgsConstructor @Data
public class LiveSession {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String titre;
 
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @Column(nullable = false)
    private LocalDateTime debut;
 
    @Column(nullable = false)
    private LocalDateTime fin;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;
    
}

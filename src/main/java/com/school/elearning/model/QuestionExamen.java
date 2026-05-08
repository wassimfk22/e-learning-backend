package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Question d'un examen — réponse libre (String).
 * Le prof corrige manuellement chaque réponse (JUSTE/FAUX).
 */
@Entity
@Table(name = "questions_examen")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuestionExamen {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String enonce;

    // Points accordés si juste
    private double points;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private ExamenModule examen;
    
    
    
}
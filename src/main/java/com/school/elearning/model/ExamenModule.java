package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Examen créé par un enseignant pour un module.
 * Contient des questions à réponse libre.
 * Corrigé manuellement par l'enseignant réponse par réponse.
 *
 * NOTE : On crée ExamenModule (distinct de l'ancienne entité Examen)
 * pour éviter les conflits avec l'héritage existant.
 */
@Entity
@Table(name = "examens_module")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExamenModule extends Evaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Durée en minutes
    private int dureeMinutes;

    private LocalDateTime dateCreation;
    
    @OneToOne
    @JoinColumn(name = "correction_id")
    private Correction correction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;

    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionExamen> questions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
    }
    
    
    
}
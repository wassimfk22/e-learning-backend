package com.school.elearning.model;

import com.school.elearning.model.enums.StatutCoursProgression;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Suivi de la consultation d'un cours par un étudiant.
 * Créé automatiquement quand l'étudiant accède à un cours pour la première fois.
 * statut : NON_COMMENCE → EN_COURS → TERMINE (quand l'étudiant marque comme terminé)
 */
@Entity
@Table(
    name = "cours_progressions",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"etudiant_id", "cours_id"},
        name = "uk_etudiant_cours"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CoursProgression {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCoursProgression statut;

    private LocalDateTime datePremierAcces;
    private LocalDateTime dateDerniereConsultation;
    private LocalDateTime dateTermine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @PrePersist
    public void prePersist() {
        if (this.datePremierAcces == null) this.datePremierAcces = LocalDateTime.now();
        if (this.statut == null) this.statut = StatutCoursProgression.EN_COURS;
    }
    
    
    
}
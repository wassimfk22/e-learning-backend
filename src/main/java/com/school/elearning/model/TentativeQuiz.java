package com.school.elearning.model;

import com.school.elearning.model.enums.StatutTentative;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le passage UNIQUE d'un étudiant à un quiz.
 * L'étudiant ne peut passer le quiz qu'une seule fois.
 * Le chrono démarre à dateDebut et expire à dateDebut + quiz.dureeMinutes.
 */
@Entity
@Table(
    name = "tentatives_quiz",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"etudiant_id", "quiz_id"},
        name = "uk_etudiant_quiz"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TentativeQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Score calculé à la soumission
    private double scoreObtenu;
    private double scoreMax;
    private double pourcentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTentative statut; // EN_COURS ou SOUMISE

    // Chrono : dateDebut = moment où l'étudiant démarre
    // dateExpiration = dateDebut + dureeMinutes (calculé à la création)
    private LocalDateTime dateDebut;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateSoumission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "tentative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReponseQuiz> reponses = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.dateDebut == null) this.dateDebut = LocalDateTime.now();
    }

    // Vérifie si le temps est écoulé
    public boolean estExpiree() {
        return LocalDateTime.now().isAfter(this.dateExpiration);
    }
    
    
    
    
}
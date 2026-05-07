package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.school.elearning.model.enums.StatutPassageExamen;

/**
 * Passage d'un examen par un étudiant (unique).
 * L'étudiant soumet ses réponses libres.
 * Le prof corrige ensuite chaque réponse (JUSTE/FAUX).
 * noteFinale calculée automatiquement après correction complète.
 */
@Entity
@Table(
    name = "passages_examen",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"etudiant_id", "examen_id"},
        name = "uk_etudiant_examen"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PassageExamen {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPassageExamen statut; // EN_COURS, SOUMIS, CORRIGE

    private LocalDateTime dateDebut;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateCorrection; // quand le prof finit la correction

    private double noteObtenue;   // calculée après correction (sur scoreMax)
    private double scoreMax;      // somme des points de toutes les questions
    private double noteFinale;    // noteObtenue ramenée sur 20

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private ExamenModule examen;

    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReponseExamenEtudiant> reponses = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.dateDebut == null) this.dateDebut = LocalDateTime.now();
    }

    public boolean estExpire() {
        return dateExpiration != null && LocalDateTime.now().isAfter(dateExpiration);
    }
    
    
    
}
// ════════════════════════════════════════════════════
// TentativeQuiz.java  →  model/
// ════════════════════════════════════════════════════
package com.school.elearning.model;
 
import com.school.elearning.model.enums.StatutTentative;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(
    name = "tentatives_quiz",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"etudiant_id", "quiz_id", "numero_tentative"},
        name = "uk_etudiant_quiz_tentative"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TentativeQuiz {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "numero_tentative", nullable = false)
    private int numeroTentative;
 
    private double scoreObtenu;
    private double scoreMax;
    private int tentativesRestantes;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTentative statut;
 
    private LocalDateTime dateDebut;
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
}
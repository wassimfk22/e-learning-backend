package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Réponse libre d'un étudiant à une question d'examen.
 * Le prof corrige en mettant estJuste = true/false.
 * pointsObtenus = question.points si estJuste, sinon 0.
 */
@Entity
@Table(
    name = "reponses_examen_etudiant",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"passage_id", "question_id"},
        name = "uk_passage_question"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReponseExamenEtudiant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Réponse libre de l'étudiant
    @Column(columnDefinition = "TEXT")
    private String reponseTexte;

    // Correction du prof : null = pas encore corrigé, true = juste, false = faux
    private Boolean estJuste;

    // Points obtenus après correction (0 si faux, question.points si juste)
    private double pointsObtenus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passage_id", nullable = false)
    private PassageExamen passage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionExamen question;
    
    
    
}
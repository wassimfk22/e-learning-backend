package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "questions_quiz")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuestionQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String enonce;

    // Les choix possibles (ex: ["Paris", "Londres", "Berlin", "Madrid"])
    @ElementCollection
    @CollectionTable(
        name = "question_quiz_choix",
        joinColumns = @JoinColumn(name = "question_quiz_id")
    )
    @Column(name = "choix")
    private List<String> choixPossibles;

    // La bonne réponse (doit être l'un des choixPossibles)
    @Column(nullable = false)
    private String bonneReponse;

    // Points accordés si bonne réponse
    private double points;

    // Appartient à un seul Quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    
    
}
package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Quiz extends Evaluation {

    // Durée en minutes que l'enseignant fixe (ex: 60 min)
    private int dureeMinutes;

    // Nombre de questions à afficher à l'étudiant (pioche aléatoire si > total questions)
    private int nombreQuestions;

    // Chaque quiz appartient à un cours
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    // Un quiz contient plusieurs questions
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionQuiz> questions = new ArrayList<>();
    
    
    
}
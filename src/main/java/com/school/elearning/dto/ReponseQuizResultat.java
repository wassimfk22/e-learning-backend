package com.school.elearning.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ReponseQuizResultat {

    private Long questionId;
    private String reponseChoisie;
    private boolean estCorrecte;
    private double pointsObtenus;
    private String bonneReponse;
    private int questionsRepondues;
    private int totalQuestions;
    private boolean toutesReponsesEnvoyees;

    // Rempli uniquement si toutesReponsesEnvoyees = true
    private ScoreFinaleResponse scoreFinale;
    
    
    
}
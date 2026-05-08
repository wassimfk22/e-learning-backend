package com.school.elearning.dto;

import com.school.elearning.model.enums.StatutProgression;
import lombok.*;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProgressionModuleResponse {

    private Long id;
    private Long moduleId;
    private String moduleTitre;
    private String niveauNom;
    private StatutProgression statut;
    private float pourcentageCompletude;
    private Date dateInscription;

    // Moyennes (calculées depuis Resultat)
    private Float moyenneQuizs;
    private Float moyenneExamens;
    private Float moyenneGenerale;

    // Stats cours du module (nouvelles)
    private int totalCours;
    private int coursTermines;
    private int coursEnCours;
    private float pourcentageCours; // % cours terminés dans ce module

    // Stats quiz du module (nouvelles)
    private int totalQuiz;
    private int quizSoumis;
    private float pourcentageQuiz;

    // Stats examens du module (nouvelles)
    private int totalExamens;
    private int examensCorrigesCount;
    private float pourcentageExamens;
    
    
    
}
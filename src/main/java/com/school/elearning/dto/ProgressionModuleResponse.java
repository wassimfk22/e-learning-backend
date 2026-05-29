package com.school.elearning.dto;

import com.school.elearning.model.enums.StatutProgression;
import lombok.*;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProgressionModuleResponse {

    // Progression
    private Long id;
    private StatutProgression statut;
    private float pourcentageCompletude;
    private Date dateInscription;

    // Module (info plate, pas l'entité)
    private Long moduleId;
    private String moduleTitre;
    private String moduleDescription;
    private String moduleDuree;

    // Niveau du module
    private Long niveauId;
    private String niveauNom;

    // Enseignant du module
    private Long enseignantId;
    private String enseignantNom;
    private String enseignantPrenom;

    // Étudiant (utile pour les vues admin/mod/ens)
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantPhoto;

    // Stats cours
    private int totalCours;
    private int coursTermines;
    private int coursEnCours;
    private float pourcentageCours;

    // Stats quiz
    private int totalQuiz;
    private int quizSoumis;
    private float pourcentageQuiz;

    // Stats examens
    private int totalExamens;
    private int examensCorrigesCount;
    private float noteMoyenneExamens;
    private float pourcentageExamens;

    // Notes (sur 20)
    private Float moyenneQuizs;
    private Float moyenneExamens;
    private Float moyenneGenerale;
}
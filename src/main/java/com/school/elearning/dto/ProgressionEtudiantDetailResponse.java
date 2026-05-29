package com.school.elearning.dto;

import com.school.elearning.model.enums.StatutProgression;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ProgressionEtudiantDetailResponse {

    // Identité étudiant
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantEmail;
    private String etudiantPhoto;
    private String niveauNom;

    // Résumé global
    private float progressionGlobale;        // 0-100%
    private int modulesInscrits;
    private int modulesTermines;

    // Détail par module
    private List<ModuleProgressionInfo> modules;

    @Data
    public static class ModuleProgressionInfo {
        private Long moduleId;
        private String moduleTitre;
        private StatutProgression statut;
        private float pourcentage;           // pourcentage global du module
        private Date dateInscription;

        // Composante cours
        private int totalCours;
        private int coursTermines;
        private int coursEnCours;
        private float pourcentageCours;

        // Composante quiz
        private int totalQuiz;
        private int quizSoumis;
        private float pourcentageQuiz;

        // Composante examens
        private int totalExamens;
        private int examensCorrigesCount;
        private float noteMoyenneExamens;    // sur 20
        private float pourcentageExamens;

        // Notes
        private float moyenneQuizs;          // sur 20
        private float moyenneExamens;        // sur 20
        private float moyenneGenerale;       // sur 20
    }
}
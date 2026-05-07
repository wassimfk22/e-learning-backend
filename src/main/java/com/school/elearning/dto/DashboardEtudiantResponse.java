package com.school.elearning.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class DashboardEtudiantResponse {

    private String nom;
    private String prenom;
    private String niveauNom;

    private int modulesInscrits;
    private int modulesTermines;

    // Progression globale combinée :
    // (% cours terminés + % quiz réussis + % examens corrigés) / 3
    private float progressionGlobale;

    private List<ProgressionModuleResponse> progressions;
    private List<TentativeResponse> derniersQuizzes;
    private List<PassageExamenResponse> derniersExamens;
    private List<CoursProgressionResponse> coursRecents;
    
    
    
}
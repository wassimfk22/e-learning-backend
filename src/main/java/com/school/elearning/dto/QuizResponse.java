package com.school.elearning.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class QuizResponse {
	
    private Long id;
    private String titre;
    private int dureeMinutes;
    private int nombreQuestions;      // nb de questions que l'étudiant verra
    private int totalQuestions;       // nb total de questions dans la banque
    private double pointsTotal;
    // Cours
    private Long coursId;
    private String coursTitre;
    // Module
    private Long moduleId;
    private String moduleTitre;
    // Enseignant
    private String enseignantNom;
    private String enseignantPrenom;
    
    
    
}
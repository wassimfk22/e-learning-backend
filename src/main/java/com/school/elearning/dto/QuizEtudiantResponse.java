package com.school.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class QuizEtudiantResponse {

    private Long id;
    private String titre;
    private int dureeMinutes;
    private int nombreQuestions;      // nb de questions que l'étudiant verra
    private double pointsTotal;
    private String coursTitre;

    // Statut pour cet étudiant
    private boolean dejaPasse;        // true = déjà soumis, ne peut plus repasser
    private boolean enCours;          // true = session active (chrono tourne)

    // Si en cours : infos de la tentative active
    private Long tentativeId;
    private LocalDateTime dateExpiration;  // dateDebut + dureeMinutes

    // Questions (sans bonneReponse)
    private List<QuestionEtudiantResponse> questions;
    
    
    
}
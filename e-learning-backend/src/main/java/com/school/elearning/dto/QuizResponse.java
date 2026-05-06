// ══════════════════════════════════════════════════════
// QuizResponse.java
// Vue liste — sans les questions détaillées
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.time.LocalDate;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String titre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreTentativesMax;
    private int nombreQuestions;        // count uniquement
    private double pointsTotal;         // somme des points de toutes les questions
    // Cours
    private Long coursId;
    private String coursTitre;
    // Module (via cours)
    private Long moduleId;
    private String moduleTitre;
    // Enseignant (via module)
    private String enseignantNom;
    private String enseignantPrenom;
    
    
    
}
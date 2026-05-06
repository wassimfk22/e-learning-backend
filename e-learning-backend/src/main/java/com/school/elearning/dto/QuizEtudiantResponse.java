// ════════════════════════════════════════════════════
// QuizEtudiantResponse.java  →  dto/
// Vue d'un quiz pour l'étudiant — SANS bonneReponse
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.time.LocalDate;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuizEtudiantResponse {
	
    private Long id;
    private String titre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreTentativesMax;
    private int tentativesDejaEffectuees;
    private int tentativesRestantes;
    private boolean peutPasser;
    private int nombreQuestions;
    private double pointsTotal;
    private String coursTitre;
    private List<QuestionEtudiantResponse> questions;
    
}
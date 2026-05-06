// ══════════════════════════════════════════════════════
// QuizDetailResponse.java
// Vue détaillée avec questions — SANS bonneReponse (pour étudiant)
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.time.LocalDate;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuizDetailResponse {
	
    private Long id;
    private String titre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreTentativesMax;
    private Long coursId;
    private String coursTitre;
    private List<QuestionQuizResponse> questions; // sans bonneReponse
    
    
    
}
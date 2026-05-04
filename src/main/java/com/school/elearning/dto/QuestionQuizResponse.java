// ══════════════════════════════════════════════════════
// QuestionQuizResponse.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuestionQuizResponse {
	
    private Long id;
    private String enonce;
    private List<String> choixPossibles;
    // ⚠️ bonneReponse NON exposée ici → l'étudiant ne doit pas la voir
    // Elle est exposée uniquement dans QuizDetailEnseignantResponse
    private double points;
    
    
    
    
}
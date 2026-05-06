// ══════════════════════════════════════════════════════
// QuestionQuizDetailEnseignantResponse.java
// Inclut la bonneReponse — pour l'enseignant uniquement
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuestionQuizDetailEnseignantResponse {
	
    private Long id;
    private String enonce;
    private List<String> choixPossibles;
    private String bonneReponse;    // ✅ visible pour l'enseignant
    private double points;
    
    
    
}
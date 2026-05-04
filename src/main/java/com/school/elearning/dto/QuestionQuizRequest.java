// ══════════════════════════════════════════════════════
// QuestionQuizRequest.java
// Utilisé dans la création du quiz pour chaque question
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuestionQuizRequest {
	
    private String enonce;                  // obligatoire
    private List<String> choixPossibles;    // obligatoire — ex: ["Paris","Londres","Berlin"]
    private String bonneReponse;            // obligatoire — doit être dans choixPossibles
    private double points;                  // obligatoire — ex: 2.0
    
    
    
}
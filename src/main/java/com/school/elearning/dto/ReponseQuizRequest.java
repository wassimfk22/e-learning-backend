// ════════════════════════════════════════════════════
// ReponseQuizRequest.java  →  dto/
// L'étudiant envoie sa réponse à UNE question
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class ReponseQuizRequest {
	
    private Long questionId;        // obligatoire
    private String reponseChoisie;  // obligatoire
    
}
// ════════════════════════════════════════════════════
// QuestionEtudiantResponse.java  →  dto/
// Question vue par l'étudiant — SANS bonneReponse
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuestionEtudiantResponse {
	
    private Long id;
    private String enonce;
    private List<String> choixPossibles;
    private double points;
    
}
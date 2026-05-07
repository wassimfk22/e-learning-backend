// ══════════════════════════════════════════════════════
// ExamenResponse.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class ExamenResponse {
	
    private Long id;
    private String titre;
    private String description;
    private int dureeMinutes;
    private LocalDateTime dateCreation;
    private Long moduleId;
    private String moduleTitre;
    private String enseignantNom;
    private String enseignantPrenom;
    private double scoreMax;
    private int nombreQuestions;
    private List<QuestionExamenResponse> questions;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class QuestionExamenResponse {
        private Long id;
        private String enonce;
        private double points;
    }
    
    
    
}
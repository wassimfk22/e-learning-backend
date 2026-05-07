package com.school.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class ScoreFinaleResponse {

    private Long tentativeId;
    private double scoreObtenu;
    private double scoreMax;
    private double pourcentage;
    private LocalDateTime dateSoumission;
    private String message;
    private boolean soumisParExpiration; // true si soumis automatiquement par timeout
    
    
    
}
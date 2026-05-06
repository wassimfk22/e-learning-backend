// ════════════════════════════════════════════════════
// ScoreFinaleResponse.java  →  dto/
// Résumé quand toutes les réponses sont envoyées
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.time.LocalDateTime;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class ScoreFinaleResponse {
	
    private Long tentativeId;
    private int numeroTentative;
    private double scoreObtenu;
    private double scoreMax;
    private double pourcentage;
    private int tentativesRestantes;
    private boolean tentativesEpuisees;
    private LocalDateTime dateSoumission;
    private String message;
    
}
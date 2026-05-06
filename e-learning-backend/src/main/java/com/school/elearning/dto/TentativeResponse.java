// ════════════════════════════════════════════════════
// TentativeResponse.java  →  dto/
// Historique d'une tentative
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import com.school.elearning.model.enums.StatutTentative;
import lombok.*;
import java.time.LocalDateTime;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class TentativeResponse {
	
    private Long id;
    private int numeroTentative;
    private double scoreObtenu;
    private double scoreMax;
    private double pourcentage;
    private int tentativesRestantes;
    private StatutTentative statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateSoumission;
    private String quizTitre;
    
}
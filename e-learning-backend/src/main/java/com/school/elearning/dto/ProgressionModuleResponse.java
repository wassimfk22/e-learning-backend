// ════════════════════════════════════════════════════
// ProgressionModuleResponse.java  →  dto/
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import com.school.elearning.model.enums.StatutProgression;
import lombok.*;
import java.util.Date;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class ProgressionModuleResponse {
	
    private Long id;
    private Long moduleId;
    private String moduleTitre;
    private String niveauNom;
    private StatutProgression statut;
    private float pourcentageCompletude;
    private Date dateInscription;
    private Float moyenneQuizs;
    private Float moyenneExamens;
    private Float moyenneGenerale;
    
}
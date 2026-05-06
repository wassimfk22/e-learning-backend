// ════════════════════════════════════════════════════
// DashboardEtudiantResponse.java  →  dto/
// ════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class DashboardEtudiantResponse {
	
    private String nom;
    private String prenom;
    private String niveauNom;
    private int modulesInscrits;
    private int modulesTermines;
    private float progressionGlobale;
    private List<ProgressionModuleResponse> progressions;
    private List<TentativeResponse> dernieresTentatives;
    
}
// ══════════════════════════════════════════════════════
// SoumissionExamenRequest.java
// L'étudiant envoie toutes ses réponses d'un coup
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class SoumissionExamenRequest {
	
    private List<ReponseItem> reponses;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ReponseItem {
        private Long questionId;
        private String reponseTexte; // réponse libre en texte
    }
    
    
    
}
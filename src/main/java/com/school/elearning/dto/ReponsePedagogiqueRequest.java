// ══════════════════════════════════════════════════════
// ReponsePedagogiqueRequest.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class ReponsePedagogiqueRequest {
	
    private String contenu;       // obligatoire
    private Long questionId;      // obligatoire — ID de la QuestionPedagogique
    
}
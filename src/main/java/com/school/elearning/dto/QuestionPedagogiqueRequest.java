// ══════════════════════════════════════════════════════
// QuestionPedagogiqueRequest.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuestionPedagogiqueRequest {
	
    private String enonce;  // obligatoire
    private Long coursId;   // obligatoire
    
}
// ══════════════════════════════════════════════════════
// CoursProgressionResponse.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;

import com.school.elearning.model.enums.StatutCoursProgression;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class CoursProgressionResponse {
	
    private Long id;
    private Long coursId;
    private String coursTitre;
    private Long moduleId;
    private String moduleTitre;
    private StatutCoursProgression statut;
    private LocalDateTime datePremierAcces;
    private LocalDateTime dateDerniereConsultation;
    private LocalDateTime dateTermine;
    
}
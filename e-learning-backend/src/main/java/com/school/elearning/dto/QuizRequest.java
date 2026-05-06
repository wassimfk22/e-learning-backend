// ══════════════════════════════════════════════════════
// QuizRequest.java
// Création d'un quiz avec ses questions en une seule fois
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuizRequest {
	
	@NotBlank
    private String titre;                           // obligatoire
	
	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;                    // obligatoire
	
	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFin;                      // obligatoire
	
	@NotNull
    private Integer nombreTentativesMax;                // obligatoire — ex: 3
	
	@NotNull
    private Long coursId;                           // obligatoire
	
	@NotNull
    private List<QuestionQuizRequest> questions;    // obligatoire — min 1 question
    
    
    
}
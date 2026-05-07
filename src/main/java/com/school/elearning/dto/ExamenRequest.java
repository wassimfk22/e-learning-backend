// ══════════════════════════════════════════════════════
// ExamenRequest.java  — création d'un examen (enseignant)
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class ExamenRequest {

    @NotBlank
    private String titre;

    private String description;

    @NotNull @Min(1)
    private Integer dureeMinutes;

    @NotNull
    private Long moduleId;

    @NotNull
    private List<QuestionExamenRequest> questions;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class QuestionExamenRequest {
        @NotBlank
        private String enonce;
        @NotNull @Min(0)
        private Double points;
    }
    
    
    
}
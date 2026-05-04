// ══════════════════════════════════════════════════════
// QuizDetailEnseignantResponse.java
// Vue complète avec bonneReponse — ENSEIGNANT uniquement
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.time.LocalDate;
import java.util.List;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuizDetailEnseignantResponse {
    private Long id;
    private String titre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreTentativesMax;
    private Long coursId;
    private String coursTitre;
    private String moduleTitre;
    private List<QuestionQuizDetailEnseignantResponse> questions; // avec bonneReponse
}
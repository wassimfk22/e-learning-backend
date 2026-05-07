package com.school.elearning.dto;

import lombok.*;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data @NoArgsConstructor @AllArgsConstructor
public class QuizRequest {

    @NotBlank
    private String titre;

    // Durée du quiz en minutes (ex: 60)
    @NotNull
    @Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    private Integer dureeMinutes;

    // Nombre de questions à afficher à l'étudiant
    // Si < total des questions saisies → pioche aléatoire
    // Si = total → toutes les questions
    @NotNull
    @Min(value = 1, message = "Il faut au moins 1 question")
    private Integer nombreQuestions;

    @NotNull
    private Long coursId;

    @NotNull
    private List<QuestionQuizRequest> questions;
}
package com.school.elearning.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class QuizDetailEnseignantResponse {
    private Long id;
    private String titre;
    private int dureeMinutes;
    private int nombreQuestions;
    private Long coursId;
    private String coursTitre;
    private String moduleTitre;
    private List<QuestionQuizDetailEnseignantResponse> questions;
}
package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreerCoursRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotNull(message = "L'ID du module est obligatoire")
    private Long moduleId;

    @NotEmpty(message = "Le cours doit avoir au moins un contenu")
    private List<ContentRequest> contents;

    @Data
    public static class ContentRequest {
        @NotNull(message = "Le type du contenu est obligatoire")
        private TypeContent type;

        @NotBlank(message = "Le contenu ne peut pas être vide")
        private String content;
    }
}
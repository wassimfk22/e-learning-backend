package com.school.elearning.dto;

import com.school.elearning.model.Content;
import com.school.elearning.model.Cours;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CoursMapper {

    public static CoursResponse toResponse(Cours cours) {
        CoursResponse dto = new CoursResponse();
        dto.setId(cours.getId());
        dto.setTitre(cours.getTitre());
        dto.setDatePublication(cours.getDatePublication());
        dto.setModuleId(cours.getModule().getId());
        dto.setModuleTitre(cours.getModule().getTitre()); // adapte selon ton modèle Module

        List<Content> contents = cours.getContents();
        if (contents != null) {
            dto.setContents(
                contents.stream()
                        .map(CoursMapper::toContentResponse)
                        .collect(Collectors.toList())
            );
        } else {
            dto.setContents(Collections.emptyList());
        }

        return dto;
    }

    public static ContentResponse toContentResponse(Content content) {
        ContentResponse dto = new ContentResponse();
        dto.setId(content.getId());
        dto.setType(content.getType());
        dto.setContent(content.getContent());
        dto.setOrdre(content.getOrdre());
        return dto;
    }
}
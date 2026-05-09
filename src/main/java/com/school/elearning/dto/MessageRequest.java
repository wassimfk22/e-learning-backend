package com.school.elearning.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class MessageRequest {

    // Contenu texte (obligatoire pour TEXT, optionnel pour IMAGE/PDF)
    private String contenu;

    // ID du message auquel on répond (null si message racine)
    private Long parentId;
    
    
    
}
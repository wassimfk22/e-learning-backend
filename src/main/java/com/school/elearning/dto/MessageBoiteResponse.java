package com.school.elearning.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class MessageBoiteResponse {

    private Long id;
    private String contenu;
    private LocalDateTime dateEnvoi;

    // Expéditeur
    private Long expediteurId;
    private String expediteurNom;
    private String expediteurPrenom;
    private String expediteurPhoto;
    private String expediteurRole;
    
}
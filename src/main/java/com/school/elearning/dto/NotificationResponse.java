package com.school.elearning.dto;

import lombok.*;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String contenu;
    private Date dateEnvoi;
    private boolean estLue;

    // Annonce liée (null si pas d'annonce)
    private Long annonceId;
    private String annonceTitre;
    
    
    
}
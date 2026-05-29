// ══════════════════════════════════════════════════════
// AnnonceResponse.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeEvenement;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class AnnonceResponse {

    private Long id;
    private String titre;
    private String contenu;
    private Date datePublication;

    // Auteur
    private Long auteurId;
    private String auteurNom;
    private String auteurPrenom;
    private String auteurRole;

    // Événement associé (null si pas d'événement)
    private EvenementInfo evenement;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class EvenementInfo {
        private Long id;
        private String titre;
        private String description;
        private LocalDateTime dateDebut;
        private LocalDateTime dateFin;
        private TypeEvenement type;
    }
    
    
    
}
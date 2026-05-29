// ══════════════════════════════════════════════════════
// AnnonceRequest.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeEvenement;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class AnnonceRequest {

    // Champs annonce
    private String titre;
    private String contenu;

    // Champs événement (optionnels — si null, pas d'événement créé)
    private String evenementTitre;
    private String evenementDescription;
    private LocalDateTime evenementDateDebut;
    private LocalDateTime evenementDateFin;
    private TypeEvenement evenementType; // COURS, EXAMEN, QUIZ, STREAM, AUTRE
    
}
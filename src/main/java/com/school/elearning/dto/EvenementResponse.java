package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeEvenement;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvenementResponse {

    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private TypeEvenement type;

    // Auteur
    private Long moderateurId;
    private String moderateurNom;
    private String moderateurPrenom;

    // Calendrier / Niveau
    private Long calendrierId;
    private Long niveauId;
    private String niveauNom;
}
package com.school.elearning.dto;

import lombok.Data;
import java.util.List;

@Data
public class CalendrierResponse {

    private Long id;

    // Niveau associé
    private Long niveauId;
    private String niveauNom;
    private String niveauFiliere;
    private String niveauAnnee;

    // Événements du calendrier
    private List<EvenementResponse> evenements;
    
    
    
}
package com.school.elearning.dto;

import com.school.elearning.model.enums.StatutTentative;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class TentativeResponse {

    private Long id;
    private double scoreObtenu;
    private double scoreMax;
    private double pourcentage;
    private StatutTentative statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateSoumission;
    private String quizTitre;
    private int dureeMinutes;

    // Temps restant en secondes (calculé à la volée, -1 si expiré/soumis)
    private long secondesRestantes;
    
    
    
}
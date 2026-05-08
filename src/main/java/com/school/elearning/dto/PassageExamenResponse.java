package com.school.elearning.dto;

import com.school.elearning.model.enums.StatutPassageExamen;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PassageExamenResponse {
	
    private Long id;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPhoto;
    private Long examenId;
    private String examenTitre;
    private StatutPassageExamen statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateCorrection;
    private double noteObtenue;
    private double scoreMax;
    private double noteFinale; // sur 20
    private long secondesRestantes;

    // Réponses (vues par l'étudiant OU le prof selon le contexte)
    private List<ReponseExamenResponse> reponses;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ReponseExamenResponse {
        private Long id;
        private Long questionId;
        private String questionEnonce;
        private double questionPoints;
        private String reponseTexte;
        private Boolean estJuste;        // null = pas encore corrigé
        private double pointsObtenus;
    }
    
}
package com.school.elearning.dto;

import lombok.*;

/**
 * Envoyé par le prof pour corriger UNE réponse d'un étudiant.
 * estJuste = true → pointsObtenus = question.points
 * estJuste = false → pointsObtenus = 0
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class CorrectionReponseRequest {
	
    private Long reponseId;   // id de la ReponseExamenEtudiant
    private boolean estJuste;
    
    
    
}
// ══════════════════════════════════════════════════════
// QuestionPedagogiqueResponse.java
// ══════════════════════════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
import java.util.Date;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class QuestionPedagogiqueResponse {
 
    private Long id;
    private String enonce;
    private Date datePost;
 
    // Étudiant
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantPhoto;
 
    // Cours
    private Long coursId;
    private String coursTitre;
 
    // Réponse du prof (null si pas encore répondu)
    private ReponsePedagogiqueInfo reponse;
 
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ReponsePedagogiqueInfo {
        private Long id;
        private String contenu;
        private Date dateReponse;
        private Long enseignantId;
        private String enseignantNom;
        private String enseignantPrenom;
    }
    
}
package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeMessageCommunaute;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String contenu;
    private TypeMessageCommunaute type;
    private String fichierUrl;
    private Date dateEnvoi;
    private Date dateModification;
    private boolean supprime;   // true = message supprimé, contenu = "[Message supprimé]"
    private boolean modifie;    // true si dateModification != null

    // Auteur
    private Long expediteurId;
    private String expediteurNom;
    private String expediteurPrenom;
    private String expediteurPhoto;

    // Réponse à (null si message racine)
    private Long parentId;
    private String parentContenuResume; // Les 50 premiers chars du message parent (aperçu)

    // Réponses directes (premier niveau seulement, pour éviter la récursion infinie)
    private List<MessageResponse> reponses;
    
    
    
}
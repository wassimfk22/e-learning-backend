// ════════════════════════════════
// UtilisateurResponse.java
// Réponse générique (sans mot de passe !)
// ════════════════════════════════
package com.school.elearning.dto;

import com.school.elearning.model.enums.Role;
import com.school.elearning.model.enums.TypeModerateur;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String photo;
    private String bio;
    private String role;
    // champs spécifiques (null si non applicable)
    private String specialite;   // Enseignant
    private String type;         // Modérateur
}
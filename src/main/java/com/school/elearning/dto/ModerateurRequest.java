// ════════════════════════════════
// ModerateurRequest.java
// ════════════════════════════════
package com.school.elearning.dto;
 
import com.school.elearning.model.enums.TypeModerateur;
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class ModerateurRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String bio;
    private TypeModerateur type;  // INGENIEURIE ou MANAGEMENT
}
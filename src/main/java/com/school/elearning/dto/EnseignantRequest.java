// ════════════════════════════════
// EnseignantRequest.java
// ════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class EnseignantRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String photo;
    private String bio;
    private String specialite;   // champ spécifique Enseignant
}
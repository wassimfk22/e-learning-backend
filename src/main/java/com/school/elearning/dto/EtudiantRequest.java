// ════════════════════════════════
// EtudiantRequest.java
// Utilisé pour créer ou modifier un étudiant
// ════════════════════════════════
package com.school.elearning.dto;
 
import lombok.*;
 
@Data @NoArgsConstructor @AllArgsConstructor
public class EtudiantRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;   // nullable en modification
    private String telephone;
    private String bio;
}
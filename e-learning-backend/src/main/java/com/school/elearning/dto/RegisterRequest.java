package com.school.elearning.dto;

import com.school.elearning.model.enums.Role;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private Role role;
    // Champs specifiques
    private String specialite; // pour Enseignant
}

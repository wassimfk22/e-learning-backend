package com.school.elearning.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private String nom;
    private String prenom;
}

package com.school.elearning.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuthRequest {
    private String email;
    private String motDePasse;
}

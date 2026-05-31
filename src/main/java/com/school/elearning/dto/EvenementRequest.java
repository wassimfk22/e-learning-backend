package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeEvenement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvenementRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    @NotNull(message = "Le type est obligatoire")
    private TypeEvenement type;

    // ID du calendrier (niveau) cible — obligatoire à la création
    private Long calendrierId;
    
    
    
}
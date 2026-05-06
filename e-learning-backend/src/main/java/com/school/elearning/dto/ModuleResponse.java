package com.school.elearning.dto;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class ModuleResponse {
	
	private Long id;
    private String titre;
    private String description;
    private String duree;
    // Niveau
    private Long niveauId;
    private String niveauNom;
    // Enseignant
    private Long enseignantId;
    private String enseignantNom;
    private String enseignantPrenom;

}

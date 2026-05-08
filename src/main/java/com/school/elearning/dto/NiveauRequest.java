package com.school.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data
public class NiveauRequest {
	
	private String nom;
	private String filiere;
	private String annee;
	private Long moderateurId;

}

package com.school.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class ModuleRequest {
	
	private String titre;
	private String description;
	private String duree;
	private Long niveauId;
	private Long enseignantId;

}

package com.school.elearning.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

public class MessageBoite {

	@Id @GeneratedValue (strategy = GenerationType.IDENTITY)
	private Long id;
	private String contenu;
	private LocalDateTime dateEnvoi;

	@ManyToOne
    @JoinColumn(name = "expediteur_id")
	private Utilisateur expediteur;
	
	@ManyToOne @JoinColumn (name = "boiteReception_id")
    private BoiteReception boiteReception;

}

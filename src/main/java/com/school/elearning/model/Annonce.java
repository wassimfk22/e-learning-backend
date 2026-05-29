package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "annonces")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Annonce {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datePublication;

    // L'annonce peut contenir un événement associé (optionnel)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "evenement_id")
    private Evenement evenement;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur auteur;

    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;
    
    
    
}
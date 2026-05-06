package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name = "communautes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Communaute {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @OneToOne @JoinColumn(name = "niveau_id")
    private Niveau niveau;

    @OneToMany(mappedBy = "communaute")
    @JsonIgnore
    private List<Etudiant> etudiants;

    @OneToMany(mappedBy = "communaute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;
    
    @ManyToOne @JoinColumn ( name = "moderateur_id" )
    @JsonIgnore
    private Moderateur moderateur;
}

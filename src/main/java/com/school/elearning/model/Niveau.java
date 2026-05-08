package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity @Table(name = "niveaux")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Niveau {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column ( nullable = false, unique = true )
    private String nom;
    
    private String filiere;
    private String annee;
    
    @ManyToOne
    @JoinColumn ( name = "moderateur_id" )
    @JsonIgnore
    private Moderateur moderateur;

    // Chaque niveau contient une liste des modules
    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Module> modules;
    
    // Chaque niveau a son calendrier de planification
    @OneToOne(mappedBy = "niveau", cascade = CascadeType.ALL)
    @JsonIgnore
    private Calendrier calendrier;

    // Chaque niveau a une communauté
    @OneToOne(mappedBy = "niveau", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Communaute communaute;
}

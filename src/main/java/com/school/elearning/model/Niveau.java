package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "niveaux")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Niveau {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String filiere;
    private String annee;

    // Chaque niveau contient une liste des modules
    @OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules;
    
    // Chaque niveau a son calendrier de planification
    @OneToOne(mappedBy = "niveau", cascade = CascadeType.ALL)
    private Calendrier calendrier;

    // Chaque niveau a une communauté
    @OneToOne(mappedBy = "niveau", cascade = CascadeType.ALL, orphanRemoval = true)
    private Communaute communaute;
}

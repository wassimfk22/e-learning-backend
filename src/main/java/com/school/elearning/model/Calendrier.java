package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "calendriers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Calendrier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chaque calendrier appartient à un niveau
    @OneToOne @JoinColumn(name = "niveau_id")
    private Niveau niveau;
    
    // Chaque calendrier contient une liste des événements
    @ManyToMany(mappedBy = "calendriers")
    private List<Evenement> evenements;
}

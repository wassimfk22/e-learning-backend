package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Module {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String description;
    private int duree;
    
    // Chaque module appartient à un seul niveau
    @ManyToOne @JoinColumn(name = "niveau_id")
    private Niveau niveau;

    // Chaque module est affecté à un seul enseignant
    @ManyToOne @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    // Chaque module contient une liste des cours
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cours> cours;

    // Chaque module contient des exams
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluation;
}

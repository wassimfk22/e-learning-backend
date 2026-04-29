package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name = "modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Module {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column ( nullable = false, unique = true )
    private String titre;
    
    private String description;
    private String duree;
    
    // Chaque module appartient à un seul niveau
    @ManyToOne @JoinColumn(name = "niveau_id")
    @JsonIgnore
    private Niveau niveau;

    // Chaque module est affecté à un seul enseignant
    @ManyToOne @JoinColumn(name = "enseignant_id")
    @JsonIgnore
    private Enseignant enseignant;

    // Chaque module contient une liste des cours
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cours> cours;

    // Chaque module contient des exams
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluation;
}

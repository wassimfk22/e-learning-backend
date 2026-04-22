package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

import com.school.elearning.model.Module;

@Entity @Table(name = "evaluations")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public abstract class Evaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    
    // Chaque evaluation va etre passee par plusieurs etudiant
    @ManyToMany (mappedBy = "evaluations")
    private List <Etudiant> etudiants;
    
    // Chaque evaluation appartient à un seul module
    @ManyToOne @JoinColumn(name = "module_id")
    private Module module;

    // Chaque evaluation aura une note final
    @OneToOne @JoinColumn (name = "note_id")
    private Note note;
}

package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "resultats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Resultat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float moyenneQuizs;
    private float moyenneExamens;
    private float moyenneGenerale;

    @ManyToOne @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne @JoinColumn(name = "module_id")
    private Module module;

    @OneToMany(mappedBy = "resultat")
    private List<Note> notes;
}

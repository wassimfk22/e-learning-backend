package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private double points;

    @Column(columnDefinition = "TEXT")
    private String enonce;

    @Column(columnDefinition = "TEXT")
    private String choixPossibles;

    @ManyToOne @JoinColumn(name = "examen_id")
    private Examen examen;

    @OneToOne @JoinColumn (name = "reponse_id")
    private ReponseEtudiant reponse;
}

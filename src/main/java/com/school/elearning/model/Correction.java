package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity @Table(name = "corrections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Correction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCorrection;
    
    @OneToOne @JoinColumn (name = "examen_id")
    private Examen examen;

//    @OneToOne @JoinColumn(name = "tentative_id")
//    private Tentative tentative;

//    @ManyToOne @JoinColumn(name = "enseignant_id")
//    private Enseignant enseignant;
}

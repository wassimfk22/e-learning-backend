package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity @Table(name = "analytiques")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Analytique {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float tauxCompletion;
    private float tempsMoyenConnexion;
    private float scoreMoyen;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateGeneration;

    @ManyToOne @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;
}

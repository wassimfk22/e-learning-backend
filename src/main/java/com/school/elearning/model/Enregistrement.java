package com.school.elearning.model;

import com.school.elearning.model.enums.StatutEnregistrement;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity @Table(name = "enregistrements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Enregistrement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnregistrement;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateVisionne;

    @Enumerated(EnumType.STRING)
    private StatutEnregistrement statut;

    @ManyToOne @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne @JoinColumn(name = "cours_id")
    private Cours cours;
}

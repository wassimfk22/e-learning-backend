package com.school.elearning.model;

import com.school.elearning.model.enums.TypeNote;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity @Table(name = "notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Note {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float valeur;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateObtention;

    @Enumerated(EnumType.STRING)
    private TypeNote type;

    @ManyToOne @JoinColumn(name = "resultat_id")
    private Resultat resultat;
    
    @OneToOne @JoinColumn (name = "evaluation_id")
    private Evaluation evaluation;
}

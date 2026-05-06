package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity @Table(name = "reponses_pedagogiques")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReponsePedagogique {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReponse;

    @ManyToOne @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @ManyToOne @JoinColumn(name = "question_pedagogique_id")
    private QuestionPedagogique questionPedagogique;
}

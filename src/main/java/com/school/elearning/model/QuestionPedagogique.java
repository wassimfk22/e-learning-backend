package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "questions_pedagogiques")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuestionPedagogique {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String enonce;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datePost;

    @ManyToOne @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne @JoinColumn(name = "cours_id")
    private Cours cours;

    // Chaque question aura une reponse
    @OneToOne @JoinColumn (name = "reponse_pedagogique_id")
    private ReponsePedagogique reponse;
}

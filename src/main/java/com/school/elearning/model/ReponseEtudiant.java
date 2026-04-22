package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "reponses_etudiants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReponseEtudiant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reponseDonnee;

    @OneToOne @JoinColumn(name = "question_id")
    private Question question;
}

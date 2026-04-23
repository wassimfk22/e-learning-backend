package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "reponses_etudiants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReponseEtudiant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String reponseDonnee;
    
    private boolean estCorrecte;

 // 🟠 CORRECTION : Question a @OneToOne @JoinColumn(reponse_id) → elle possède la FK
    // Donc ici on met mappedBy pour éviter 2 colonnes FK en base
    @OneToOne(mappedBy = "reponse")
    private Question question;
}

package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "etudiants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Etudiant extends Utilisateur {
    @Temporal(TemporalType.DATE)
    private Date dateInscription;

    @OneToMany(mappedBy = "etudiant")
    private List<ProgressionModule> progressions;

    @OneToMany(mappedBy = "etudiant")
    private List<Enregistrement> enregistrements;

    @OneToMany(mappedBy = "etudiant")
    private List<QuestionPedagogique> questionsPedagogiques;
    
    @ManyToMany @JoinTable ( name = "etudiant_evaluations", 
    		joinColumns = @JoinColumn(name = "etudiant_id"),
    		inverseJoinColumns = @JoinColumn(name = "evaluation_id"))
    private List<Evaluation> evaluations;
    
//    @ManyToMany(mappedBy = "etudiants")
//    private List<Stream> streams;

    @ManyToOne @JoinColumn(name = "communaute_id")
    private Communaute communaute;
}

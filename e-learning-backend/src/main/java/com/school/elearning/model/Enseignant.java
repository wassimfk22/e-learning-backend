package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "enseignants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Enseignant extends Utilisateur {
    private String specialite;

    @OneToMany(mappedBy = "enseignant")
    private List<Module> modules;

    @OneToMany(mappedBy = "enseignant")
    private List<Correction> corrections;

    @OneToMany(mappedBy = "enseignant")
    private List<ReponsePedagogique> reponsesPedagogiques;

//    @OneToMany(mappedBy = "enseignant")
//    private List<Stream> streams;
}

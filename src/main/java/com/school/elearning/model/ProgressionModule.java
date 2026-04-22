package com.school.elearning.model;

import com.school.elearning.model.enums.StatutProgression;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity @Table(name = "progressions_modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProgressionModule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date dateInscription;

    @Enumerated(EnumType.STRING)
    private StatutProgression statut;

    private float pourcentageCompletude;

    @ManyToOne @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne @JoinColumn(name = "module_id")
    private Module module;
}

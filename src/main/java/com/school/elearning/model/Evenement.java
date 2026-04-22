package com.school.elearning.model;

import com.school.elearning.model.enums.TypeEvenement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "evenements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Evenement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateDebut;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    private TypeEvenement type;

    // Chaque événement peut etre dans plusieurs calendriers
    @ManyToMany @JoinTable(name = "evenement_calendriers",
        joinColumns = @JoinColumn(name = "evenement_id"),
        inverseJoinColumns = @JoinColumn(name = "calendrier_id"))
    private List<Calendrier> calendriers;

    @ManyToOne @JoinColumn(name = "moderateur_id")
    private Moderateur moderateur;
}

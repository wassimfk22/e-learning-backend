package com.school.elearning.model;

import com.school.elearning.model.enums.TypeEvenement;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evenements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 🔴 CORRECTION : @Temporal est INCOMPATIBLE avec LocalDateTime → supprimé
    // LocalDateTime est géré nativement par JPA sans @Temporal
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    private TypeEvenement type;

    @ManyToMany
    @JoinTable(
        name = "evenement_calendriers",
        joinColumns = @JoinColumn(name = "evenement_id"),
        inverseJoinColumns = @JoinColumn(name = "calendrier_id")
    )
    private List<Calendrier> calendriers;

    @ManyToOne
    @JoinColumn(name = "moderateur_id")
    private Moderateur moderateur;
}

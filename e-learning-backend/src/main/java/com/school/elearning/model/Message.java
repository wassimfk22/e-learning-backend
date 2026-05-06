package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;

    // Plusieurs meesages peuvent etre envoyé par un utilisateur 
    @ManyToOne @JoinColumn(name = "expediteur_id")
    private Utilisateur expediteur;

    // Plusieurs messages peuvent appartenir à une communauté
    @ManyToOne @JoinColumn(name = "communaute_id")
    private Communaute communaute;
}

package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages_boite")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MessageBoite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private LocalDateTime dateEnvoi;

    @ManyToOne
    @JoinColumn(name = "expediteur_id")
    private Utilisateur expediteur;

    @ManyToOne
    @JoinColumn(name = "boite_reception_id")
    private BoiteReception boiteReception;

    @PrePersist
    public void prePersist() {
        if (this.dateEnvoi == null) {
            this.dateEnvoi = LocalDateTime.now();
        }
    }
}

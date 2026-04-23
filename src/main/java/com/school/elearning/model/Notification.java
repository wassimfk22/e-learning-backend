package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;
    
    @Column ( nullable =  false )
    private boolean EstLue;

    @ManyToOne @JoinColumn(name = "annonce_id")
    private Annonce annonce;

    @ManyToMany (mappedBy = "notifications")
    private List <Utilisateur> destinataires;
}

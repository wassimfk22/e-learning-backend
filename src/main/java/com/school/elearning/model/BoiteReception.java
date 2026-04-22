package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "boites_reception")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BoiteReception {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @OneToOne @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @OneToMany (mappedBy = "boiteReception") 
    private List<MessageBoite> messagesBoite;
}

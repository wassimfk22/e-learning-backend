package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;

    @Column(nullable = false)
    private boolean estLue = false;

    // La notification appartient à UN seul utilisateur destinataire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    // La notification est liée à une annonce
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id")
    private Annonce annonce;
}
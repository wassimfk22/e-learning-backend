package com.school.elearning.model;

import com.school.elearning.model.enums.TypeMessageCommunaute;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contenu texte du message (peut être null si c'est juste un fichier)
    @Column(columnDefinition = "TEXT")
    private String contenu;

    // Type : TEXT, IMAGE, PDF
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMessageCommunaute type = TypeMessageCommunaute.TEXT;

    // URL/chemin du fichier si type = IMAGE ou PDF
    private String fichierUrl;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;

    // Date de dernière modification (null si jamais modifié)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModification;

    // true si le message a été supprimé (soft delete pour garder la structure des réponses)
    @Column(nullable = false)
    private boolean supprime = false;

    // L'auteur du message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Utilisateur expediteur;

    // La communauté à laquelle appartient le message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "communaute_id", nullable = false)
    private Communaute communaute;

    // Le message parent (null si c'est un message racine)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Message parent;

    // Réponses à ce message (supprimées en cascade si le parent est supprimé)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> reponses;
}
package com.school.elearning.model;

import com.school.elearning.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(
    name = "utilisateurs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "telephone"})
    }
)
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public abstract class Utilisateur {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private String photo;
    private String bio;

    @Column(unique = true, nullable = false)
    private String telephone;

    @OneToMany(mappedBy = "auteur")
    private List<Annonce> annonces;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private BoiteReception boiteReception;

    // MODIFIÉ : Notification simple par utilisateur (plus de ManyToMany)
    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;
}
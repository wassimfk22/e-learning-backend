package com.school.elearning.service;

import com.school.elearning.dto.CalendrierResponse;
import com.school.elearning.dto.EvenementRequest;
import com.school.elearning.dto.EvenementResponse;
import com.school.elearning.model.*;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendrierService {

    private final CalendrierRepository calendrierRepository;
    private final EvenementRepository evenementRepository;
    private final ModerateurRepository moderateurRepository;
    private final AdministrateurRepository administrateurRepository;
    private final NiveauRepository niveauRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;

    // ══════════════════════════════════════════════════════════════
    // LECTURE — accessible à tous les authentifiés
    // ══════════════════════════════════════════════════════════════

    /** Calendrier d'un niveau par son ID */
    public CalendrierResponse getCalendrierParNiveau(Long niveauId) {
        Calendrier calendrier = calendrierRepository.findByNiveauId(niveauId)
                .orElseThrow(() -> new RuntimeException("Calendrier introuvable pour le niveau : " + niveauId));
        return toCalendrierResponse(calendrier);
    }

    /** Calendrier du niveau de l'utilisateur connecté (étudiant ou enseignant) */
    public CalendrierResponse getMonCalendrier(Authentication auth) {
        Utilisateur utilisateur = getUtilisateurConnecte(auth);
        Long niveauId = getNiveauId(utilisateur);

        Calendrier calendrier = calendrierRepository.findByNiveauId(niveauId)
                .orElseThrow(() -> new RuntimeException("Aucun calendrier associé à votre niveau."));

        return toCalendrierResponse(calendrier);
    }

    /** Tous les calendriers — pour admin */
    public List<CalendrierResponse> getTousLesCalendriers() {
        return calendrierRepository.findAll().stream()
                .map(this::toCalendrierResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // CRUD ÉVÉNEMENTS — modérateur/admin uniquement
    // ══════════════════════════════════════════════════════════════

    /** Créer un événement dans un calendrier */
    @Transactional
    public EvenementResponse creerEvenement(EvenementRequest request, Authentication auth) {
        Utilisateur auteur = getUtilisateurConnecte(auth);
        validerDates(request.getDateDebut(), request.getDateFin());

        Calendrier calendrier = calendrierRepository.findById(request.getCalendrierId())
                .orElseThrow(() -> new RuntimeException("Calendrier introuvable : " + request.getCalendrierId()));

        verifierAccesCalendrier(auteur, calendrier);

        Moderateur moderateur = getModerateurOuNull(auteur);

        Evenement evenement = new Evenement();
        evenement.setTitre(request.getTitre());
        evenement.setDescription(request.getDescription());
        evenement.setDateDebut(request.getDateDebut());
        evenement.setDateFin(request.getDateFin());
        evenement.setType(request.getType());
        evenement.setModerateur(moderateur);
        evenement.setCalendriers(List.of(calendrier));

        Evenement saved = evenementRepository.save(evenement);
        log.info("Événement '{}' créé dans le calendrier du niveau '{}'",
                saved.getTitre(), calendrier.getNiveau().getNom());

        // Notifier tous les utilisateurs du niveau
        notifierUtilisateursNiveau(calendrier,
                "📅 Nouvel événement : " + saved.getTitre()
                        + " — " + saved.getType().name()
                        + " le " + saved.getDateDebut().toLocalDate(),
                auteur);

        return toEvenementResponse(saved, calendrier);
    }

    /** Modifier un événement */
    @Transactional
    public EvenementResponse modifierEvenement(Long evenementId, EvenementRequest request,
                                                Authentication auth) {
        Utilisateur auteur = getUtilisateurConnecte(auth);
        validerDates(request.getDateDebut(), request.getDateFin());

        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable : " + evenementId));

        // Vérifier l'accès via le calendrier existant
        Calendrier calendrier = evenement.getCalendriers().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Cet événement n'est associé à aucun calendrier."));

        verifierAccesCalendrier(auteur, calendrier);

        evenement.setTitre(request.getTitre());
        evenement.setDescription(request.getDescription());
        evenement.setDateDebut(request.getDateDebut());
        evenement.setDateFin(request.getDateFin());
        evenement.setType(request.getType());

        Evenement saved = evenementRepository.save(evenement);
        log.info("Événement '{}' modifié", saved.getTitre());

        // Notifier
        notifierUtilisateursNiveau(calendrier,
                "✏️ Événement modifié : " + saved.getTitre()
                        + " — Nouvelle date : " + saved.getDateDebut().toLocalDate(),
                auteur);

        return toEvenementResponse(saved, calendrier);
    }

    /** Supprimer un événement */
    @Transactional
    public void supprimerEvenement(Long evenementId, Authentication auth) {
        Utilisateur auteur = getUtilisateurConnecte(auth);

        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable : " + evenementId));

        Calendrier calendrier = evenement.getCalendriers().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Cet événement n'est associé à aucun calendrier."));

        verifierAccesCalendrier(auteur, calendrier);

        String titreSauvegarde = evenement.getTitre();
        evenementRepository.deleteById(evenementId);
        log.info("Événement '{}' supprimé", titreSauvegarde);

        // Notifier
        notifierUtilisateursNiveau(calendrier,
                "🗑️ Événement annulé : " + titreSauvegarde,
                auteur);
    }

    /** Détail d'un événement */
    public EvenementResponse getEvenement(Long evenementId) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable : " + evenementId));
        Calendrier calendrier = evenement.getCalendriers().stream().findFirst().orElse(null);
        return toEvenementResponse(evenement, calendrier);
    }

    // ══════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════

    private void notifierUtilisateursNiveau(Calendrier calendrier, String contenu,
                                             Utilisateur expediteur) {
        if (calendrier.getNiveau() == null) return;
        Long niveauId = calendrier.getNiveau().getId();

        List<Utilisateur> destinataires = new ArrayList<>();

        // Étudiants du niveau (via leur communauté)
        List<Etudiant> etudiants = etudiantRepository.findAll().stream()
                .filter(e -> e.getCommunaute() != null
                        && e.getCommunaute().getNiveau() != null
                        && e.getCommunaute().getNiveau().getId().equals(niveauId))
                .collect(Collectors.toList());
        destinataires.addAll(etudiants);

        // Enseignants dont au moins un module appartient à ce niveau
        List<Enseignant> enseignants = enseignantRepository.findAll().stream()
                .filter(ens -> ens.getModules() != null
                        && ens.getModules().stream()
                            .anyMatch(m -> m.getNiveau() != null
                                    && m.getNiveau().getId().equals(niveauId)))
                .collect(Collectors.toList());
        destinataires.addAll(enseignants);

        // Ne pas notifier l'auteur lui-même
        destinataires.removeIf(u -> u.getId().equals(expediteur.getId()));

        for (Utilisateur destinataire : destinataires) {
            Notification notif = new Notification();
            notif.setContenu(contenu);
            notif.setDateEnvoi(new Date());
            notif.setEstLue(false);
            notif.setDestinataire(destinataire);
            notificationRepository.save(notif);
        }

        log.info("Notification calendrier envoyée à {} utilisateurs du niveau '{}'",
                destinataires.size(), calendrier.getNiveau().getNom());
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private void verifierAccesCalendrier(Utilisateur auteur, Calendrier calendrier) {
        // Admin : accès total
        if (auteur.getRole() == Role.ADMIN) return;

        // Modérateur : doit être responsable du niveau du calendrier
        if (auteur.getRole() == Role.MODERATEUR) {
            if (calendrier.getNiveau() == null
                    || calendrier.getNiveau().getModerateur() == null
                    || !calendrier.getNiveau().getModerateur().getId().equals(auteur.getId())) {
                throw new RuntimeException("Accès refusé : ce calendrier ne vous appartient pas.");
            }
            return;
        }

        throw new RuntimeException("Accès refusé : rôle insuffisant.");
    }

    private void validerDates(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null)
            throw new RuntimeException("Les dates de début et de fin sont obligatoires.");
        if (!fin.isAfter(debut))
            throw new RuntimeException("La date de fin doit être après la date de début.");
    }

    private Long getNiveauId(Utilisateur utilisateur) {
        if (utilisateur instanceof Etudiant etudiant) {
            if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null)
                throw new RuntimeException("Vous n'êtes affecté à aucun niveau.");
            return etudiant.getCommunaute().getNiveau().getId();
        }
        if (utilisateur instanceof Enseignant enseignant) {
            if (enseignant.getModules() == null || enseignant.getModules().isEmpty())
                throw new RuntimeException("Vous n'avez aucun module assigné.");
            return enseignant.getModules().get(0).getNiveau().getId();
        }
        throw new RuntimeException("Rôle non supporté pour cette opération.");
    }

    private Moderateur getModerateurOuNull(Utilisateur utilisateur) {
        if (utilisateur instanceof Moderateur) {
            return moderateurRepository.findById(utilisateur.getId()).orElse(null);
        }
        return null;
    }

    private Utilisateur getUtilisateurConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return utilisateurRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPERS
    // ══════════════════════════════════════════════════════════════

    private CalendrierResponse toCalendrierResponse(Calendrier c) {
        CalendrierResponse r = new CalendrierResponse();
        r.setId(c.getId());

        if (c.getNiveau() != null) {
            r.setNiveauId(c.getNiveau().getId());
            r.setNiveauNom(c.getNiveau().getNom());
            r.setNiveauFiliere(c.getNiveau().getFiliere());
            r.setNiveauAnnee(c.getNiveau().getAnnee());
        }

        List<Evenement> evenements = evenementRepository
                .findByCalendrierIdOrderByDateDebut(c.getId());

        r.setEvenements(evenements.stream()
                .map(e -> toEvenementResponse(e, c))
                .collect(Collectors.toList()));

        return r;
    }

    public EvenementResponse toEvenementResponse(Evenement e, Calendrier calendrier) {
        EvenementResponse r = new EvenementResponse();
        r.setId(e.getId());
        r.setTitre(e.getTitre());
        r.setDescription(e.getDescription());
        r.setDateDebut(e.getDateDebut());
        r.setDateFin(e.getDateFin());
        r.setType(e.getType());

        if (e.getModerateur() != null) {
            r.setModerateurId(e.getModerateur().getId());
            r.setModerateurNom(e.getModerateur().getNom());
            r.setModerateurPrenom(e.getModerateur().getPrenom());
        }

        if (calendrier != null) {
            r.setCalendrierId(calendrier.getId());
            if (calendrier.getNiveau() != null) {
                r.setNiveauId(calendrier.getNiveau().getId());
                r.setNiveauNom(calendrier.getNiveau().getNom());
            }
        }

        return r;
    }
    
    
    
}
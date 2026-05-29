package com.school.elearning.service;

import com.school.elearning.dto.AnnonceRequest;
import com.school.elearning.dto.AnnonceResponse;
import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final MessageBoiteRepository messageBoiteRepository;         // pour boite de reception
    private final BoiteReceptionRepository boiteReceptionRepository;

    // ══════════════════════════════════════════════════════════════
    // 1. LISTER TOUTES LES ANNONCES (lecture pour tous les auth.)
    // ══════════════════════════════════════════════════════════════

    public List<AnnonceResponse> getToutesAnnonces() {
        return annonceRepository.findAllByOrderByDatePublicationDesc()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 2. DÉTAIL D'UNE ANNONCE
    // ══════════════════════════════════════════════════════════════

    public AnnonceResponse getAnnonceById(Long id) {
        return toResponse(findAnnonce(id));
    }

    // ══════════════════════════════════════════════════════════════
    // 3. PUBLIER UNE ANNONCE (Admin + Modérateur)
    //    → Crée l'annonce + éventuellement un événement
    //    → Envoie une notification à TOUS les utilisateurs
    //    → Envoie le message dans la boite de réception de TOUS
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public AnnonceResponse publierAnnonce(AnnonceRequest request, Authentication auth) {
        Utilisateur auteur = getUtilisateurConnecte(auth);

        if (request.getTitre() == null || request.getTitre().isBlank())
            throw new RuntimeException("Le titre de l'annonce est obligatoire.");
        if (request.getContenu() == null || request.getContenu().isBlank())
            throw new RuntimeException("Le contenu de l'annonce est obligatoire.");

        Annonce annonce = new Annonce();
        annonce.setTitre(request.getTitre());
        annonce.setContenu(request.getContenu());
        annonce.setDatePublication(new Date());
        annonce.setAuteur(auteur);

        // Créer l'événement associé si les infos sont fournies
        if (request.getEvenementTitre() != null && !request.getEvenementTitre().isBlank()) {
            Evenement evenement = new Evenement();
            evenement.setTitre(request.getEvenementTitre());
            evenement.setDescription(request.getEvenementDescription());
            evenement.setDateDebut(request.getEvenementDateDebut());
            evenement.setDateFin(request.getEvenementDateFin());
            evenement.setType(request.getEvenementType());
            annonce.setEvenement(evenement);
        }

        Annonce saved = annonceRepository.save(annonce);

        // Diffuser à tous les utilisateurs
        diffuserAuxUtilisateurs(saved, auteur);

        return toResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════
    // 4. MODIFIER UNE ANNONCE (auteur uniquement)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public AnnonceResponse modifierAnnonce(Long id, AnnonceRequest request, Authentication auth) {
        Utilisateur auteur = getUtilisateurConnecte(auth);
        Annonce annonce = findAnnonce(id);
        verifierAuteur(annonce, auteur);

        annonce.setTitre(request.getTitre());
        annonce.setContenu(request.getContenu());

        // Mettre à jour ou créer l'événement
        if (request.getEvenementTitre() != null && !request.getEvenementTitre().isBlank()) {
            Evenement evenement = annonce.getEvenement() != null
                    ? annonce.getEvenement() : new Evenement();
            evenement.setTitre(request.getEvenementTitre());
            evenement.setDescription(request.getEvenementDescription());
            evenement.setDateDebut(request.getEvenementDateDebut());
            evenement.setDateFin(request.getEvenementDateFin());
            evenement.setType(request.getEvenementType());
            annonce.setEvenement(evenement);
        } else {
            // Supprimer l'événement si aucune info fournie
            annonce.setEvenement(null);
        }

        return toResponse(annonceRepository.save(annonce));
    }

    // ══════════════════════════════════════════════════════════════
    // 5. SUPPRIMER UNE ANNONCE (auteur uniquement)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public void supprimerAnnonce(Long id, Authentication auth) {
        Utilisateur auteur = getUtilisateurConnecte(auth);
        Annonce annonce = findAnnonce(id);
        verifierAuteur(annonce, auteur);
        annonceRepository.deleteById(id);
    }

    // ══════════════════════════════════════════════════════════════
    // DIFFUSION : Notification + Boite de réception
    // ══════════════════════════════════════════════════════════════

    private void diffuserAuxUtilisateurs(Annonce annonce, Utilisateur auteur) {
        List<Utilisateur> tousUtilisateurs = utilisateurRepository.findAll();

        String contenuMessage = "📢 " + annonce.getTitre() + "\n\n" + annonce.getContenu();
        if (annonce.getEvenement() != null) {
            Evenement ev = annonce.getEvenement();
            contenuMessage += "\n\n📅 Événement : " + ev.getTitre();
            if (ev.getDateDebut() != null)
                contenuMessage += " — " + ev.getDateDebut();
        }

        for (Utilisateur utilisateur : tousUtilisateurs) {
            // 1. Notification
            Notification notif = new Notification();
            notif.setContenu("Nouvelle annonce : " + annonce.getTitre());
            notif.setDateEnvoi(new Date());
            notif.setEstLue(false);
            notif.setDestinataire(utilisateur);
            notif.setAnnonce(annonce);
            notificationRepository.save(notif);

            // 2. Boite de réception
            if (utilisateur.getBoiteReception() != null) {
                MessageBoite messageBoite = new MessageBoite();
                messageBoite.setContenu(contenuMessage);
                messageBoite.setExpediteur(auteur);
                messageBoite.setBoiteReception(utilisateur.getBoiteReception());
                messageBoiteRepository.save(messageBoite);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private Annonce findAnnonce(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce introuvable : " + id));
    }

    private Utilisateur getUtilisateurConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return utilisateurRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private void verifierAuteur(Annonce annonce, Utilisateur utilisateur) {
        if (!annonce.getAuteur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous ne pouvez modifier/supprimer que vos propres annonces.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPER
    // ══════════════════════════════════════════════════════════════

    public AnnonceResponse toResponse(Annonce a) {
        AnnonceResponse r = new AnnonceResponse();
        r.setId(a.getId());
        r.setTitre(a.getTitre());
        r.setContenu(a.getContenu());
        r.setDatePublication(a.getDatePublication());
        r.setAuteurId(a.getAuteur().getId());
        r.setAuteurNom(a.getAuteur().getNom());
        r.setAuteurPrenom(a.getAuteur().getPrenom());
        r.setAuteurRole(a.getAuteur().getRole().name());

        if (a.getEvenement() != null) {
            Evenement ev = a.getEvenement();
            AnnonceResponse.EvenementInfo info = new AnnonceResponse.EvenementInfo();
            info.setId(ev.getId());
            info.setTitre(ev.getTitre());
            info.setDescription(ev.getDescription());
            info.setDateDebut(ev.getDateDebut());
            info.setDateFin(ev.getDateFin());
            info.setType(ev.getType());
            r.setEvenement(info);
        }

        return r;
    }
    
    
    
}
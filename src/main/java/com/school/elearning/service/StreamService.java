package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.model.enums.RoleLive;
import com.school.elearning.model.enums.SessionStatus;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository liveSessionRepository;
    private final EnseignantRepository enseignantRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ─── SESSION ────────────────────────────────────────────────────

    /** Créer une LiveSession — réservé à l'enseignant connecté */
    @Transactional
    public LiveSession creerSession(String titre, String description,
                                    LocalDateTime debut, LocalDateTime fin,
                                    Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);

        LiveSession session = new LiveSession();
        session.setTitre(titre);
        session.setDescription(description);
        session.setDebut(debut);
        session.setFin(fin);
        session.setStatus(SessionStatus.PLANIFIE);
        session.setEnseignant(enseignant);

        return liveSessionRepository.save(session);
    }

    /** Changer le status d'une session (PLANIFIE → EN_COURS → TERMINE) */
    @Transactional
    public LiveSession changerStatus(Long sessionId, SessionStatus nouveauStatus, Authentication auth) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        Enseignant enseignant = getEnseignantConnecte(auth);
        if (!session.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Vous n'êtes pas le propriétaire de cette session");
        }

        session.setStatus(nouveauStatus);
        return liveSessionRepository.save(session);
    }

    /** Lister toutes les sessions d'un enseignant */
    public List<LiveSession> getMesSessions(Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        return liveSessionRepository.findByEnseignant(enseignant);
    }

    /** Lister les sessions EN_COURS — pour les étudiants */
    public List<LiveSession> getSessionsActives() {
        return liveSessionRepository.findByStatus(SessionStatus.EN_COURS);
    }

    /** Lister toutes les sessions PLANIFIEES */
    public List<LiveSession> getSessionsPlanifiees() {
        return liveSessionRepository.findByStatus(SessionStatus.PLANIFIE);
    }

    // ─── HELPER ─────────────────────────────────────────────────────

    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUtilisateur().getId();
        return enseignantRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur n'est pas un enseignant"));
    }
}
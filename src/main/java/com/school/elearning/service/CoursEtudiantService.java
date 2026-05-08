package com.school.elearning.service;

import com.school.elearning.dto.CoursProgressionResponse;
import com.school.elearning.dto.CoursResponse;
import com.school.elearning.dto.CoursMapper;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.StatutCoursProgression;
import com.school.elearning.model.enums.StatutProgression;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursEtudiantService {

    private final CoursRepository coursRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursProgressionRepository coursProgressionRepository;
    private final ProgressionModuleRepository progressionModuleRepository;
    private final ModuleRepository moduleRepository;

    // ══════════════════════════════════════════════════════════════
    // 1. ACCÉDER À UN COURS
    //    - Auto-inscrit l'étudiant au module si pas encore inscrit
    //    - Crée ou met à jour sa CoursProgression
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public CoursResponse accederCours(Long coursId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + coursId));

        verifierAccesCours(etudiant, cours);

        // ── Auto-inscription au module ──────────────────────────
        autoInscrireAuModule(etudiant, cours.getModule());

        // ── Créer ou mettre à jour la CoursProgression ──────────
        CoursProgression cp = coursProgressionRepository
                .findByEtudiantIdAndCoursId(etudiant.getId(), coursId)
                .orElseGet(() -> {
                    CoursProgression nouveau = new CoursProgression();
                    nouveau.setEtudiant(etudiant);
                    nouveau.setCours(cours);
                    nouveau.setStatut(StatutCoursProgression.EN_COURS);
                    return nouveau;
                });

        cp.setDateDerniereConsultation(LocalDateTime.now());
        coursProgressionRepository.save(cp);

        // Recalculer la progression du module
        mettreAJourProgressionModule(etudiant, cours.getModule());

        return CoursMapper.toResponse(cours);
    }

    // ══════════════════════════════════════════════════════════════
    // 2. MARQUER UN COURS COMME TERMINÉ
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public CoursProgressionResponse marquerTermine(Long coursId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);

        CoursProgression cp = coursProgressionRepository
                .findByEtudiantIdAndCoursId(etudiant.getId(), coursId)
                .orElseThrow(() -> new RuntimeException(
                        "Vous n'avez pas encore accédé à ce cours. Consultez-le d'abord."));

        cp.setStatut(StatutCoursProgression.TERMINE);
        cp.setDateTermine(LocalDateTime.now());
        coursProgressionRepository.save(cp);

        // Recalculer la progression du module
        mettreAJourProgressionModule(etudiant, cp.getCours().getModule());

        return toResponse(cp);
    }

    // ══════════════════════════════════════════════════════════════
    // 3. LISTER LES COURS D'UN MODULE (avec statut pour l'étudiant)
    // ══════════════════════════════════════════════════════════════

    public List<CoursAvecStatutResponse> getCoursModuleAvecStatut(Long moduleId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        verifierAccesModule(etudiant, module);

        return coursRepository.findByModule(module).stream().map(cours -> {
            CoursAvecStatutResponse r = new CoursAvecStatutResponse();
            r.setCours(CoursMapper.toResponse(cours));
            coursProgressionRepository
                    .findByEtudiantIdAndCoursId(etudiant.getId(), cours.getId())
                    .ifPresentOrElse(
                            cp -> {
                                r.setStatut(cp.getStatut());
                                r.setDateDerniereConsultation(cp.getDateDerniereConsultation());
                                r.setDateTermine(cp.getDateTermine());
                            },
                            () -> r.setStatut(null)
                    );
            return r;
        }).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 4. MA PROGRESSION COURS (tous modules)
    // ══════════════════════════════════════════════════════════════

    public List<CoursProgressionResponse> getMaProgressionCours(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        return coursProgressionRepository.findByEtudiant(etudiant)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // AUTO-INSCRIPTION AU MODULE
    // Crée une ProgressionModule si elle n'existe pas encore
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public void autoInscrireAuModule(Etudiant etudiant, Module module) {
        boolean dejaInscrit = progressionModuleRepository.existsByEtudiantAndModule(etudiant, module);
        if (!dejaInscrit) {
            ProgressionModule progression = new ProgressionModule();
            progression.setEtudiant(etudiant);
            progression.setModule(module);
            progression.setDateInscription(new Date());
            progression.setStatut(StatutProgression.EN_COURS);
            progression.setPourcentageCompletude(0f);
            progressionModuleRepository.save(progression);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MISE À JOUR PROGRESSION MODULE basée sur les cours terminés
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public void mettreAJourProgressionModule(Etudiant etudiant, Module module) {
        // S'assurer que l'inscription existe
        autoInscrireAuModule(etudiant, module);

        progressionModuleRepository.findByEtudiant(etudiant).stream()
                .filter(p -> p.getModule().getId().equals(module.getId()))
                .findFirst()
                .ifPresent(progression -> {
                    long totalCours = module.getCours() != null ? module.getCours().size() : 0;
                    if (totalCours == 0) return;

                    long coursTermines = coursProgressionRepository
                            .findTerminesParModuleId(etudiant, module.getId()).size();

                    float completudeCours = (float) coursTermines / totalCours * 100f;

                    // On prend le max pour ne pas réduire si quiz/examens ont déjà avancé
                    progression.setPourcentageCompletude(
                            Math.max(progression.getPourcentageCompletude(), completudeCours));

                    if (completudeCours >= 100f) {
                        progression.setStatut(StatutProgression.TERMINE);
                    } else if (progression.getStatut() == StatutProgression.NON_COMMENCE) {
                        progression.setStatut(StatutProgression.EN_COURS);
                    }
                    progressionModuleRepository.save(progression);
                });
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private void verifierAccesCours(Etudiant etudiant, Cours cours) {
        if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null) {
            throw new RuntimeException("Vous n'êtes affecté à aucun niveau.");
        }
        Long niveauId = etudiant.getCommunaute().getNiveau().getId();
        if (!cours.getModule().getNiveau().getId().equals(niveauId)) {
            throw new RuntimeException("Accès refusé : ce cours n'appartient pas à votre niveau.");
        }
    }

    private void verifierAccesModule(Etudiant etudiant, Module module) {
        if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null) {
            throw new RuntimeException("Vous n'êtes affecté à aucun niveau.");
        }
        Long niveauId = etudiant.getCommunaute().getNiveau().getId();
        if (!module.getNiveau().getId().equals(niveauId)) {
            throw new RuntimeException("Accès refusé : ce module n'appartient pas à votre niveau.");
        }
    }

    private Etudiant getEtudiantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));
    }

    public CoursProgressionResponse toResponse(CoursProgression cp) {
        CoursProgressionResponse r = new CoursProgressionResponse();
        r.setId(cp.getId());
        r.setCoursId(cp.getCours().getId());
        r.setCoursTitre(cp.getCours().getTitre());
        r.setModuleId(cp.getCours().getModule().getId());
        r.setModuleTitre(cp.getCours().getModule().getTitre());
        r.setStatut(cp.getStatut());
        r.setDatePremierAcces(cp.getDatePremierAcces());
        r.setDateDerniereConsultation(cp.getDateDerniereConsultation());
        r.setDateTermine(cp.getDateTermine());
        return r;
    }

    @lombok.Data
    public static class CoursAvecStatutResponse {
        private CoursResponse cours;
        private StatutCoursProgression statut;
        private LocalDateTime dateDerniereConsultation;
        private LocalDateTime dateTermine;
    }
    
    
    
}
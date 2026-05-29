package com.school.elearning.service;

import com.school.elearning.dto.ProgressionModuleResponse;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.StatutProgression;
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
public class ProgressionService {

    private final ProgressionModuleRepository progressionRepository;
    private final EtudiantRepository etudiantRepository;
    private final ModuleRepository moduleRepository;
    private final ProgressionCalculatorService calculatorService;
    private final ResultatRepository resultatRepository;

    /** Inscrire manuellement un étudiant à un module */
    @Transactional
    public ProgressionModuleResponse inscrireModule(Long moduleId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        boolean existe = progressionRepository.existsByEtudiantAndModule(etudiant, module);
        if (existe) throw new RuntimeException("Déjà inscrit à ce module");

        ProgressionModule progression = new ProgressionModule();
        progression.setEtudiant(etudiant);
        progression.setModule(module);
        progression.setDateInscription(new Date());
        progression.setStatut(StatutProgression.EN_COURS);
        progression.setPourcentageCompletude(0f);

        ProgressionModule saved = progressionRepository.save(progression);
        return enrichir(saved, etudiant);
    }

    /** Ma progression — recalculée automatiquement */
    @Transactional
    public List<ProgressionModuleResponse> getMaProgression(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);

        // Recalcul avant retour
        progressionRepository.findByEtudiant(etudiant).forEach(pm ->
                calculatorService.recalculerEtSauvegarder(etudiant, pm.getModule()));

        return progressionRepository.findByEtudiant(etudiant).stream()
                .map(pm -> enrichir(pm, etudiant))
                .collect(Collectors.toList());
    }

    /** Progression d'un étudiant — pour admin/mod/enseignant */
    @Transactional
    public List<ProgressionModuleResponse> getProgressionEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable : " + etudiantId));

        progressionRepository.findByEtudiant(etudiant).forEach(pm ->
                calculatorService.recalculerEtSauvegarder(etudiant, pm.getModule()));

        return progressionRepository.findByEtudiant(etudiant).stream()
                .map(pm -> enrichir(pm, etudiant))
                .collect(Collectors.toList());
    }

    /** Progression de tous les étudiants d'un module */
    @Transactional
    public List<ProgressionModuleResponse> getProgressionParModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable : " + moduleId));

        return progressionRepository.findByModule(module).stream()
                .map(pm -> {
                    calculatorService.recalculerEtSauvegarder(pm.getEtudiant(), module);
                    ProgressionModule updated = progressionRepository
                            .findByEtudiantAndModule(pm.getEtudiant(), module)
                            .orElse(pm);
                    return enrichir(updated, pm.getEtudiant());
                })
                .collect(Collectors.toList());
    }

    // ── ENRICHISSEMENT — DTO complet avec stats ───────────────────

    private ProgressionModuleResponse enrichir(ProgressionModule pm, Etudiant etudiant) {
        // Mapping de base (champs plats)
        ProgressionModuleResponse r = ProgressionAdminService.toDto(pm);

        // Ajout des détails par composante
        ProgressionCalculatorService.ProgressionDetailParComposante detail =
                calculatorService.getDetailComposantes(etudiant, pm.getModule());

        r.setTotalCours(detail.getTotalCours());
        r.setCoursTermines(detail.getCoursTermines());
        r.setCoursEnCours(detail.getCoursEnCours());
        r.setPourcentageCours(detail.getPourcentageCours());

        r.setTotalQuiz(detail.getTotalQuiz());
        r.setQuizSoumis(detail.getQuizSoumis());
        r.setPourcentageQuiz(detail.getPourcentageQuiz());

        r.setTotalExamens(detail.getTotalExamens());
        r.setExamensCorrigesCount(detail.getExamensCorrigesCount());
        r.setNoteMoyenneExamens(detail.getNoteMoyenneExamens());
        r.setPourcentageExamens(detail.getPourcentageExamens());

        // Notes depuis Resultat
        resultatRepository
                .findByEtudiantIdAndModuleId(etudiant.getId(), pm.getModule().getId())
                .ifPresent(res -> {
                    r.setMoyenneQuizs(res.getMoyenneQuizs());
                    r.setMoyenneExamens(res.getMoyenneExamens());
                    r.setMoyenneGenerale(res.getMoyenneGenerale());
                });

        return r;
    }

    // ── HELPER ────────────────────────────────────────────────────

    private Etudiant getEtudiantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));
    }
}
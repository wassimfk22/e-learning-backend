package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressionCalculatorService {

    private final ProgressionModuleRepository progressionModuleRepository;
    private final CoursProgressionRepository coursProgressionRepository;
    private final TentativeQuizRepository tentativeQuizRepository;
    private final PassageExamenRepository passageExamenRepository;
    private final ExamenModuleRepository examenModuleRepository;

    // ══════════════════════════════════════════════════════════════
    // RECALCUL ET SAUVEGARDE — à appeler après chaque action
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ProgressionModule recalculerEtSauvegarder(Etudiant etudiant, Module module) {
        float pourcentage = calculerPourcentageModule(etudiant, module);

        ProgressionModule pm = progressionModuleRepository
                .findByEtudiantAndModule(etudiant, module)
                .orElseGet(() -> {
                    ProgressionModule nouveau = new ProgressionModule();
                    nouveau.setEtudiant(etudiant);
                    nouveau.setModule(module);
                    nouveau.setDateInscription(new Date());
                    nouveau.setStatut(StatutProgression.NON_COMMENCE);
                    nouveau.setPourcentageCompletude(0f);
                    return nouveau;
                });

        pm.setPourcentageCompletude(pourcentage);
        pm.setStatut(
            pourcentage >= 100f ? StatutProgression.TERMINE :
            pourcentage >   0f  ? StatutProgression.EN_COURS :
                                   StatutProgression.NON_COMMENCE
        );

        log.debug("Progression recalculée — étudiant={} module={} → {}%",
                etudiant.getId(), module.getTitre(), pourcentage);

        return progressionModuleRepository.save(pm);
    }

    // ══════════════════════════════════════════════════════════════
    // CALCUL POURCENTAGE D'UN MODULE
    // Formule : moyenne des composantes existantes
    //   - Cours    : (cours terminés    / total cours)   * 100
    //   - Quiz     : (quiz soumis       / total quiz)    * 100
    //   - Examens  : (note moy /20 * 100) si corrigés, sinon 0
    // ══════════════════════════════════════════════════════════════

    public float calculerPourcentageModule(Etudiant etudiant, Module module) {
        float totalComposantes = 0f;
        int nbComposantes = 0;

        // ── Composante cours ──────────────────────────────────────
        int totalCours = module.getCours() != null ? module.getCours().size() : 0;
        if (totalCours > 0) {
            long termines = coursProgressionRepository
                    .findTerminesParModuleId(etudiant, module.getId()).size();
            float pct = (float) termines / totalCours * 100f;
            totalComposantes += pct;
            nbComposantes++;
        }

        // ── Composante quiz ───────────────────────────────────────
        long totalQuiz = module.getCours() != null
                ? module.getCours().stream()
                    .flatMap(c -> c.getQuizzes() != null
                            ? c.getQuizzes().stream()
                            : java.util.stream.Stream.empty())
                    .count()
                : 0L;
        if (totalQuiz > 0) {
            long soumis = tentativeQuizRepository
                    .findByEtudiantAndModuleId(etudiant, module.getId()).stream()
                    .filter(t -> t.getStatut() == StatutTentative.SOUMISE)
                    .count();
            float pct = (float) soumis / totalQuiz * 100f;
            totalComposantes += pct;
            nbComposantes++;
        }

        // ── Composante examens ────────────────────────────────────
        long totalExamens = examenModuleRepository.findByModuleId(module.getId()).size();
        if (totalExamens > 0) {
            List<PassageExamen> corriges = passageExamenRepository
                    .findCorigesParModuleId(etudiant, module.getId());
            float pctExamens = 0f;
            if (!corriges.isEmpty()) {
                // Note moyenne ramenée sur 100
                pctExamens = (float) corriges.stream()
                        .mapToDouble(p -> p.getNoteFinale() / 20.0 * 100.0)
                        .average().orElse(0);
            }
            // On compte la composante même si 0 (étudiant n'a pas encore passé)
            totalComposantes += pctExamens;
            nbComposantes++;
        }

        if (nbComposantes == 0) return 0f;

        float resultat = totalComposantes / nbComposantes;
        return Math.min(100f, Math.round(resultat * 100f) / 100f);
    }

    // ══════════════════════════════════════════════════════════════
    // PROGRESSION GLOBALE — tous modules confondus
    // ══════════════════════════════════════════════════════════════

    public float calculerProgressionGlobale(Etudiant etudiant) {
        List<ProgressionModule> progressions = progressionModuleRepository.findByEtudiant(etudiant);
        if (progressions.isEmpty()) return 0f;

        float total = 0f;
        for (ProgressionModule pm : progressions) {
            total += calculerPourcentageModule(etudiant, pm.getModule());
        }

        float resultat = total / progressions.size();
        return Math.min(100f, Math.round(resultat * 100f) / 100f);
    }

    // ══════════════════════════════════════════════════════════════
    // DÉTAIL PAR COMPOSANTE — pour affichage riche
    // ══════════════════════════════════════════════════════════════

    public ProgressionDetailParComposante getDetailComposantes(Etudiant etudiant, Module module) {
        ProgressionDetailParComposante detail = new ProgressionDetailParComposante();

        // Cours
        int totalCours = module.getCours() != null ? module.getCours().size() : 0;
        long coursTermines = totalCours > 0
                ? coursProgressionRepository.findTerminesParModuleId(etudiant, module.getId()).size()
                : 0;
        long coursConsultes = totalCours > 0
                ? coursProgressionRepository.countByEtudiantAndModuleId(etudiant, module.getId())
                : 0;
        detail.setTotalCours(totalCours);
        detail.setCoursTermines((int) coursTermines);
        detail.setCoursEnCours((int) Math.max(0, coursConsultes - coursTermines));
        detail.setPourcentageCours(totalCours > 0 ? (float) coursTermines / totalCours * 100f : 0f);

        // Quiz
        long totalQuiz = module.getCours() != null
                ? module.getCours().stream()
                    .flatMap(c -> c.getQuizzes() != null
                            ? c.getQuizzes().stream()
                            : java.util.stream.Stream.empty())
                    .count()
                : 0L;
        long quizSoumis = totalQuiz > 0
                ? tentativeQuizRepository.findByEtudiantAndModuleId(etudiant, module.getId())
                    .stream().filter(t -> t.getStatut() == StatutTentative.SOUMISE).count()
                : 0L;
        detail.setTotalQuiz((int) totalQuiz);
        detail.setQuizSoumis((int) quizSoumis);
        detail.setPourcentageQuiz(totalQuiz > 0 ? (float) quizSoumis / totalQuiz * 100f : 0f);

        // Examens
        int totalExamens = (int) examenModuleRepository.findByModuleId(module.getId()).size();
        List<PassageExamen> corriges = passageExamenRepository
                .findCorigesParModuleId(etudiant, module.getId());
        float noteMoyenneExamens = corriges.isEmpty() ? 0f :
                (float) corriges.stream().mapToDouble(PassageExamen::getNoteFinale).average().orElse(0);
        detail.setTotalExamens(totalExamens);
        detail.setExamensCorrigesCount(corriges.size());
        detail.setNoteMoyenneExamens(noteMoyenneExamens);
        detail.setPourcentageExamens(totalExamens > 0
                ? (float) corriges.size() / totalExamens * 100f : 0f);

        // Global module
        detail.setPourcentageGlobal(calculerPourcentageModule(etudiant, module));

        return detail;
    }

    // ── DTO interne ───────────────────────────────────────────────
    @lombok.Data
    public static class ProgressionDetailParComposante {
        // Cours
        private int totalCours;
        private int coursTermines;
        private int coursEnCours;
        private float pourcentageCours;
        // Quiz
        private int totalQuiz;
        private int quizSoumis;
        private float pourcentageQuiz;
        // Examens
        private int totalExamens;
        private int examensCorrigesCount;
        private float noteMoyenneExamens;
        private float pourcentageExamens;
        // Global
        private float pourcentageGlobal;
    }
    
    
    
}
package com.school.elearning.service;

import com.school.elearning.dto.ProgressionEtudiantDetailResponse;
import com.school.elearning.dto.ProgressionModuleResponse;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressionAdminService {

    private final EtudiantRepository etudiantRepository;
    private final ModuleRepository moduleRepository;
    private final NiveauRepository niveauRepository;
    private final ProgressionModuleRepository progressionModuleRepository;
    private final ResultatRepository resultatRepository;
    private final ProgressionCalculatorService calculatorService;

    /** Progression complète d'un étudiant — tous ses modules */
    @Transactional
    public ProgressionEtudiantDetailResponse getProgressionDetailleeEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable : " + etudiantId));

        // Recalculer toutes les progressions avant de retourner
        List<ProgressionModule> progressions = progressionModuleRepository.findByEtudiant(etudiant);
        progressions.forEach(pm ->
                calculatorService.recalculerEtSauvegarder(etudiant, pm.getModule()));

        // Recharger après recalcul
        progressions = progressionModuleRepository.findByEtudiant(etudiant);

        return buildResponse(etudiant, progressions);
    }

    /** Progression de tous les étudiants d'un module */
    @Transactional
    public List<ProgressionEtudiantDetailResponse> getProgressionParModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable : " + moduleId));

        List<ProgressionModule> toutesProgressions = progressionModuleRepository.findByModule(module);

        return toutesProgressions.stream().map(pm -> {
            Etudiant etudiant = pm.getEtudiant();
            calculatorService.recalculerEtSauvegarder(etudiant, module);
            List<ProgressionModule> progressions = progressionModuleRepository.findByEtudiant(etudiant);
            return buildResponse(etudiant, progressions);
        }).collect(Collectors.toList());
    }

    /** Progression de tous les étudiants d'un niveau */
    @Transactional
    public List<ProgressionEtudiantDetailResponse> getProgressionParNiveau(Long niveauId) {
        niveauRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + niveauId));

        List<ProgressionModule> toutesProgressions =
                progressionModuleRepository.findByNiveauId(niveauId);

        // Dédupliquer les étudiants
        List<Etudiant> etudiants = toutesProgressions.stream()
                .map(ProgressionModule::getEtudiant)
                .distinct()
                .collect(Collectors.toList());

        return etudiants.stream().map(etudiant -> {
            List<ProgressionModule> progressions = progressionModuleRepository.findByEtudiant(etudiant);
            progressions.forEach(pm ->
                    calculatorService.recalculerEtSauvegarder(etudiant, pm.getModule()));
            return buildResponse(etudiant,
                    progressionModuleRepository.findByEtudiant(etudiant));
        }).collect(Collectors.toList());
    }

    // ── MAPPER ────────────────────────────────────────────────────

    private ProgressionEtudiantDetailResponse buildResponse(Etudiant etudiant,
                                                             List<ProgressionModule> progressions) {
        ProgressionEtudiantDetailResponse r = new ProgressionEtudiantDetailResponse();
        r.setEtudiantId(etudiant.getId());
        r.setEtudiantNom(etudiant.getNom());
        r.setEtudiantPrenom(etudiant.getPrenom());
        r.setEtudiantEmail(etudiant.getEmail());
        r.setEtudiantPhoto(etudiant.getPhoto());

        if (etudiant.getCommunaute() != null && etudiant.getCommunaute().getNiveau() != null) {
            r.setNiveauNom(etudiant.getCommunaute().getNiveau().getNom());
        }

        r.setProgressionGlobale(calculatorService.calculerProgressionGlobale(etudiant));

        List<ProgressionEtudiantDetailResponse.ModuleProgressionInfo> modulesInfo =
                progressions.stream().map(pm -> {
                    ProgressionEtudiantDetailResponse.ModuleProgressionInfo info =
                            new ProgressionEtudiantDetailResponse.ModuleProgressionInfo();

                    info.setModuleId(pm.getModule().getId());
                    info.setModuleTitre(pm.getModule().getTitre());
                    info.setStatut(pm.getStatut());
                    info.setPourcentage(pm.getPourcentageCompletude());
                    info.setDateInscription(pm.getDateInscription());

                    // Détail par composante
                    ProgressionCalculatorService.ProgressionDetailParComposante detail =
                            calculatorService.getDetailComposantes(etudiant, pm.getModule());
                    info.setTotalCours(detail.getTotalCours());
                    info.setCoursTermines(detail.getCoursTermines());
                    info.setCoursEnCours(detail.getCoursEnCours());
                    info.setPourcentageCours(detail.getPourcentageCours());
                    info.setTotalQuiz(detail.getTotalQuiz());
                    info.setQuizSoumis(detail.getQuizSoumis());
                    info.setPourcentageQuiz(detail.getPourcentageQuiz());
                    info.setTotalExamens(detail.getTotalExamens());
                    info.setExamensCorrigesCount(detail.getExamensCorrigesCount());
                    info.setNoteMoyenneExamens(detail.getNoteMoyenneExamens());
                    info.setPourcentageExamens(detail.getPourcentageExamens());

                    // Résultat (notes)
                    resultatRepository
                            .findByEtudiantIdAndModuleId(etudiant.getId(), pm.getModule().getId())
                            .ifPresent(res -> {
                                info.setMoyenneQuizs(res.getMoyenneQuizs());
                                info.setMoyenneExamens(res.getMoyenneExamens());
                                info.setMoyenneGenerale(res.getMoyenneGenerale());
                            });

                    return info;
                }).collect(Collectors.toList());

        r.setModules(modulesInfo);
        r.setModulesInscrits(progressions.size());
        r.setModulesTermines((int) progressions.stream()
                .filter(pm -> pm.getStatut() == com.school.elearning.model.enums.StatutProgression.TERMINE)
                .count());

        return r;
    }
    
    public static ProgressionModuleResponse toDto(ProgressionModule pm) {
        ProgressionModuleResponse r = new ProgressionModuleResponse();
        r.setId(pm.getId());
        r.setStatut(pm.getStatut());
        r.setPourcentageCompletude(pm.getPourcentageCompletude());
        r.setDateInscription(pm.getDateInscription());

        if (pm.getModule() != null) {
            r.setModuleId(pm.getModule().getId());
            r.setModuleTitre(pm.getModule().getTitre());
            r.setModuleDescription(pm.getModule().getDescription());
            r.setModuleDuree(pm.getModule().getDuree());

            if (pm.getModule().getNiveau() != null) {
                r.setNiveauId(pm.getModule().getNiveau().getId());
                r.setNiveauNom(pm.getModule().getNiveau().getNom());
            }
            if (pm.getModule().getEnseignant() != null) {
                r.setEnseignantId(pm.getModule().getEnseignant().getId());
                r.setEnseignantNom(pm.getModule().getEnseignant().getNom());
                r.setEnseignantPrenom(pm.getModule().getEnseignant().getPrenom());
            }
        }

        if (pm.getEtudiant() != null) {
            r.setEtudiantId(pm.getEtudiant().getId());
            r.setEtudiantNom(pm.getEtudiant().getNom());
            r.setEtudiantPrenom(pm.getEtudiant().getPrenom());
            r.setEtudiantPhoto(pm.getEtudiant().getPhoto());
        }

        return r;
    }
    
}
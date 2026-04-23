// ════════════════════════════════════════════════════
// ProgressionService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
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
 
@Service
@RequiredArgsConstructor
public class ProgressionService {
 
    private final ProgressionModuleRepository progressionRepository;
    private final EtudiantRepository etudiantRepository;
    private final ModuleRepository moduleRepository;
 
    /** Inscrire un étudiant à un module */
    @Transactional
    public ProgressionModule inscrireModule(Long moduleId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));
 
        // Vérifier doublon
        boolean existe = progressionRepository.existsByEtudiantAndModule(etudiant, module);
        if (existe) throw new RuntimeException("Déjà inscrit à ce module");
 
        ProgressionModule progression = new ProgressionModule();
        progression.setEtudiant(etudiant);
        progression.setModule(module);
        progression.setDateInscription(new Date());
        progression.setStatut(StatutProgression.EN_COURS);
        progression.setPourcentageCompletude(0f);
        return progressionRepository.save(progression);
    }
 
    /** Mettre à jour la progression d'un étudiant */
    @Transactional
    public ProgressionModule mettreAJour(Long progressionId, float pourcentage,
                                          Authentication auth) {
        ProgressionModule progression = progressionRepository.findById(progressionId)
                .orElseThrow(() -> new RuntimeException("Progression introuvable"));
 
        Etudiant etudiant = getEtudiantConnecte(auth);
        if (!progression.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Ce n'est pas votre progression");
        }
 
        progression.setPourcentageCompletude(pourcentage);
        if (pourcentage >= 100f) {
            progression.setStatut(StatutProgression.TERMINE);
        }
        return progressionRepository.save(progression);
    }
 
    /** Récupérer la progression d'un étudiant connecté */
    public List<ProgressionModule> getMaProgression(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        return progressionRepository.findByEtudiant(etudiant);
    }
 
    private Etudiant getEtudiantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));
    }
}
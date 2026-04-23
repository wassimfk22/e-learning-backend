// ════════════════════════════════════════════════════
// CoursService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
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
public class CoursService {
 
    private final CoursRepository coursRepository;
    private final ModuleRepository moduleRepository;
    private final EnseignantRepository enseignantRepository;
 
    public List<Cours> getTousCours() {
        return coursRepository.findAll();
    }
 
    public Cours getCoursById(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + id));
    }
 
    public List<Cours> getCoursByModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));
        return coursRepository.findByModule(module);
    }
 
    @Transactional
    public Cours creerCours(String titre, Long moduleId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));
 
        // Vérifier que l'enseignant est bien responsable du module
        if (!module.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce module ne vous appartient pas");
        }
 
        Cours cours = new Cours();
        cours.setTitre(titre);
        cours.setModule(module);
        cours.setDatePublication(new Date());
        return coursRepository.save(cours);
    }
 
    @Transactional
    public Cours modifierCours(Long id, String titre, Authentication auth) {
        Cours cours = getCoursById(id);
        Enseignant enseignant = getEnseignantConnecte(auth);
 
        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce cours ne vous appartient pas");
        }
        cours.setTitre(titre);
        return coursRepository.save(cours);
    }
 
    @Transactional
    public void supprimerCours(Long id, Authentication auth) {
        Cours cours = getCoursById(id);
        Enseignant enseignant = getEnseignantConnecte(auth);
 
        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce cours ne vous appartient pas");
        }
        coursRepository.delete(cours);
    }
 
    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un enseignant"));
    }
}
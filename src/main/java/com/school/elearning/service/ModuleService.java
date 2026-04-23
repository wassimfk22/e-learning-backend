// ════════════════════════════════════════════════════
// ModuleService.java
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
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class ModuleService {
 
    private final ModuleRepository moduleRepository;
    private final NiveauRepository niveauRepository;
    private final EnseignantRepository enseignantRepository;
 
    public List<Module> getTousModules() {
        return moduleRepository.findAll();
    }
 
    public Module getModuleById(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module introuvable : " + id));
    }
 
    public List<Module> getModulesByNiveau(Long niveauId) {
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable"));
        return moduleRepository.findByNiveau(niveau);
    }
 
    public List<Module> getMesModules(Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        return moduleRepository.findByEnseignant(enseignant);
    }
 
    @Transactional
    public Module creerModule(String titre, String description, int duree,
                               Long niveauId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable"));
 
        Module module = new Module();
        module.setTitre(titre);
        module.setDescription(description);
        module.setDuree(duree);
        module.setNiveau(niveau);
        module.setEnseignant(enseignant);
        return moduleRepository.save(module);
    }
 
    @Transactional
    public Module modifierModule(Long id, String titre, String description,
                                  int duree, Authentication auth) {
        Module module = getModuleById(id);
        Enseignant enseignant = getEnseignantConnecte(auth);
 
        if (!module.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce module ne vous appartient pas");
        }
        module.setTitre(titre);
        module.setDescription(description);
        module.setDuree(duree);
        return moduleRepository.save(module);
    }
 
    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un enseignant"));
    }
    
    
    
}
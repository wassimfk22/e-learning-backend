// ════════════════════════════════════════════════════
// NiveauService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
import com.school.elearning.model.Niveau;
import com.school.elearning.repository.NiveauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class NiveauService {
 
    private final NiveauRepository niveauRepository;
 
    public List<Niveau> getTousNiveaux() {
        return niveauRepository.findAll();
    }
 
    public Niveau getNiveauById(Long id) {
        return niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + id));
    }
 
    @Transactional
    public Niveau creerNiveau(String nom, String filiere, String annee) {
        Niveau niveau = new Niveau();
        niveau.setNom(nom);
        niveau.setFiliere(filiere);
        niveau.setAnnee(annee);
        return niveauRepository.save(niveau);
    }
 
    @Transactional
    public Niveau modifierNiveau(Long id, String nom, String filiere, String annee) {
        Niveau niveau = getNiveauById(id);
        niveau.setNom(nom);
        niveau.setFiliere(filiere);
        niveau.setAnnee(annee);
        return niveauRepository.save(niveau);
    }
 
    @Transactional
    public void supprimerNiveau(Long id) {
        niveauRepository.deleteById(id);
    }
}
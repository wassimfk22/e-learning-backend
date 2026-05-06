// ════════════════════════════════════════════════════
// NiveauService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
import com.school.elearning.model.Calendrier;
import com.school.elearning.model.Communaute;
import com.school.elearning.model.Enseignant;
import com.school.elearning.model.Moderateur;
import com.school.elearning.model.Niveau;
import com.school.elearning.repository.ModerateurRepository;
import com.school.elearning.repository.NiveauRepository;
import com.school.elearning.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class NiveauService {
 
    private final NiveauRepository niveauRepository;
    private final ModerateurRepository moderateurRepository;
 
    public List<Niveau> getTousNiveaux() {
        return niveauRepository.findAll();
    }
 
    public Niveau getNiveauById(Long id) {
        return niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + id));
    }
 
    @Transactional
    public Niveau creerNiveau(Authentication auth, String nom, String filiere, String annee) {
    	Moderateur moderateur = getModerateurConnecte(auth);
        verifierNiveauUnique(nom);

        // 1. Création du Niveau
        Niveau niveau = new Niveau();
        niveau.setNom(nom);
        niveau.setFiliere(filiere);
        niveau.setAnnee(annee);

        // 2. Création de la Communauté
        Communaute communaute = new Communaute();
        communaute.setNom("Communauté " + nom);
        communaute.setDescription("Espace d'échange pour " + nom);
        communaute.setDateCreation(new java.util.Date());
        communaute.setModerateur(moderateur);
        
        // Liaison bidirectionnelle Niveau <-> Communauté
        communaute.setNiveau(niveau);
        niveau.setCommunaute(communaute);

        // 3. Création du Calendrier (Nouveau !)
        Calendrier calendrier = new Calendrier();
        
        // Liaison bidirectionnelle Niveau <-> Calendrier
        calendrier.setNiveau(niveau);
        niveau.setCalendrier(calendrier);

        // 4. Sauvegarde unique du Niveau
        // Grâce au CascadeType.ALL, Hibernate sauvegarde aussi la Communauté et le Calendrier
        return niveauRepository.save(niveau);
    }
 
    @Transactional
    public Niveau modifierNiveau(Long id, String nom, String filiere, String annee) {
        Niveau niveau = getNiveauById(id);
     // Vérifier l'unicité SEULEMENT si on change le nom
        if (!niveau.getNom().equalsIgnoreCase(nom)) {
            verifierNiveauUnique(nom);
            niveau.setNom(nom);
            
            // On met à jour la communauté car le nom a changé
            if (niveau.getCommunaute() != null) {
                niveau.getCommunaute().setNom("Communauté " + nom);
                niveau.getCommunaute().setDescription("Espace d'échange pour " + nom);
            }
        }
        niveau.setFiliere(filiere);
        niveau.setAnnee(annee);
        return niveauRepository.save(niveau);
    }
 
    @Transactional
    public void supprimerNiveau(Long id) {
        niveauRepository.deleteById(id);
    }
    
    private void verifierNiveauUnique(String nom) {
        if (niveauRepository.existsByNom(nom)) {
        	throw new RuntimeException("Niveau existe déjà : " + nom);	
        }
    }
    
    private Moderateur getModerateurConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return this.moderateurRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ou pas un modérateur"));
    }
    
}
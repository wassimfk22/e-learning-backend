// ════════════════════════════════════════════════════
// NiveauService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
import com.school.elearning.dto.NiveauRequest;
import com.school.elearning.model.Administrateur;
import com.school.elearning.model.Calendrier;
import com.school.elearning.model.Communaute;
import com.school.elearning.model.Enseignant;
import com.school.elearning.model.Moderateur;
import com.school.elearning.model.Niveau;
import com.school.elearning.model.Utilisateur;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.AdministrateurRepository;
import com.school.elearning.repository.ModerateurRepository;
import com.school.elearning.repository.NiveauRepository;
import com.school.elearning.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
 
@Service
@RequiredArgsConstructor
public class NiveauService {
 
    private final NiveauRepository niveauRepository;
    private final ModerateurRepository moderateurRepository;
    private final AdministrateurRepository administrateurRepository;
 
    public List<Niveau> getTousNiveaux() {
        return niveauRepository.findAll();
    }
 
    public Niveau getNiveauById(Long id) {
        return niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + id));
    }
 
    @Transactional
    public Niveau creerNiveau(Authentication auth, NiveauRequest request) {
        Utilisateur utilisateur = (Utilisateur) getPersonnelConnecte(auth);
        
        // On récupère le rôle (qui est un Enum)
        Role userRole = utilisateur.getRole();

        // 1. Vérification de sécurité avec l'Enum
        // On compare avec Role.ADMIN et Role.MODERATEUR
        if (userRole != Role.ADMIN && userRole != Role.MODERATEUR) {
            throw new RuntimeException("Accès refusé : Seuls les Admins ou Modérateurs peuvent créer un niveau.");
        }

        // 2. Détermination du modérateur responsable
        Moderateur moderateurFinal;

        if (userRole == Role.ADMIN) {
            // L'Admin doit spécifier quel modérateur est affecté
            if (request.getModerateurId() == null) {
                throw new RuntimeException("L'Admin doit spécifier un ID de modérateur.");
            }
            moderateurFinal = this.moderateurRepository.findById(request.getModerateurId())
                    .orElseThrow(() -> new RuntimeException("Modérateur spécifié introuvable !"));
        } else {
            // Si c'est un Modérateur, il s'affecte lui-même
            // On récupère l'ID de l'utilisateur connecté
            moderateurFinal = this.moderateurRepository.findById(utilisateur.getId())
                    .orElseThrow(() -> new RuntimeException("Votre profil modérateur est introuvable !"));
        }

        // 3. Suite de la création (Inchangée)
        verifierNiveauUnique(request.getNom());

        Niveau niveau = new Niveau();
        niveau.setNom(request.getNom());
        niveau.setFiliere(request.getFiliere());
        niveau.setAnnee(request.getAnnee());
        niveau.setModerateur(moderateurFinal); // On lie le modérateur au niveau

        // 5. Création de la Communauté
        Communaute communaute = new Communaute();
        communaute.setNom("Communauté " + request.getNom());
        communaute.setDescription("Espace d'échange pour " + request.getNom());
        communaute.setDateCreation(new java.util.Date());
        communaute.setModerateur(moderateurFinal);
        
        // Liaison bidirectionnelle
        communaute.setNiveau(niveau);
        niveau.setCommunaute(communaute);

        // 6. Création du Calendrier
        Calendrier calendrier = new Calendrier();
        calendrier.setNiveau(niveau);
        niveau.setCalendrier(calendrier);

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
    
    private Object getPersonnelConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Long userId = details.getUtilisateur().getId();

        // 1. On cherche d'abord si c'est un Admin
        Optional<Administrateur> admin = administrateurRepository.findById(userId);
        if (admin.isPresent()) return admin.get();

        // 2. Sinon on cherche si c'est un Modérateur
        Optional<Moderateur> moderateur = moderateurRepository.findById(userId);
        if (moderateur.isPresent()) return moderateur.get();

        throw new RuntimeException("Accès refusé : Ni Admin, ni Modérateur");
    }
    
}
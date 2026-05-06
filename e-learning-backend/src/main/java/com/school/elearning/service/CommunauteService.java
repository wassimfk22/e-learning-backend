package com.school.elearning.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.school.elearning.model.Communaute;
import com.school.elearning.model.Moderateur;
import com.school.elearning.model.Niveau;
import com.school.elearning.repository.CommunauteRepository;
import com.school.elearning.repository.ModerateurRepository;
import com.school.elearning.repository.NiveauRepository;
import com.school.elearning.security.CustomUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunauteService {

	private final ModerateurRepository moderateurRepository;
    private final NiveauRepository niveauRepository;
    private final CommunauteRepository communauteRepository;

    @Transactional
    public Communaute associerANiveau(Authentication auth, Long niveauId) {
    	Moderateur moderateur = getModerateurConnecte(auth);
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable"));
        
     // SÉCURITÉ : On vérifie si le niveau n'a pas déjà une communauté
        if (niveau.getCommunaute() != null) {
            throw new RuntimeException("Le niveau '" + niveau.getNom() + "' possède déjà une communauté.");
        }

        Communaute communaute = new Communaute();
        communaute.setNom("Communauté " + niveau.getNom());
        communaute.setDescription("Espace d'échange officiel pour le niveau " + niveau.getNom());
        communaute.setModerateur(moderateur);
        communaute.setDateCreation(new java.util.Date());

        // 3. Établissement de la relation bidirectionnelle
        communaute.setNiveau(niveau);
        niveau.setCommunaute(communaute);

        return communauteRepository.save(communaute);
    }
    
    private Moderateur getModerateurConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return this.moderateurRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ou pas un modérateur"));
    }
    
    
    
}

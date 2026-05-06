// ════════════════════════════════════════════════════
// AnnonceService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
import com.school.elearning.model.*;
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
public class AnnonceService {
 
    private final AnnonceRepository annonceRepository;
    private final ModerateurRepository moderateurRepository;
 
    public List<Annonce> getToutesAnnonces() {
        return annonceRepository.findAll();
    }
 
    @Transactional
    public Annonce publierAnnonce(String titre, String contenu, Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Moderateur moderateur = moderateurRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un modérateur"));
 
        Annonce annonce = new Annonce();
        annonce.setTitre(titre);
        annonce.setContenu(contenu);
        annonce.setDatePublication(new Date());
        annonce.setAuteur(moderateur);
        return annonceRepository.save(annonce);
    }
}
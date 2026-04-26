package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final ModerateurRepository moderateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    // ══════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════

    public List<UtilisateurResponse> getTousUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getTousEtudiants() {
        return etudiantRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getTousEnseignants() {
        return enseignantRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getTousModerateurs() {
        return moderateurRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public UtilisateurResponse getUtilisateurById(Long id) {
        return toResponse(findUtilisateur(id));
    }

    // ══════════════════════════════════════════════
    // CREATE ÉTUDIANT
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse creerEtudiant(EtudiantRequest req) {
        verifierEmailUnique(req.getEmail());
        verifierTelephoneUnique(req.getTelephone());

        Etudiant e = new Etudiant();
        e.setNom(req.getNom()); e.setPrenom(req.getPrenom());
        e.setEmail(req.getEmail());
        e.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        e.setTelephone(req.getTelephone()); e.setBio(req.getBio());
        e.setRole(Role.ETUDIANT); e.setDateInscription(new Date());
        return toResponse(etudiantRepository.save(e));
    }

    // ══════════════════════════════════════════════
    // CREATE ENSEIGNANT
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse creerEnseignant(EnseignantRequest req) {
        verifierEmailUnique(req.getEmail());
        verifierTelephoneUnique(req.getTelephone());

        Enseignant en = new Enseignant();
        en.setNom(req.getNom()); en.setPrenom(req.getPrenom());
        en.setEmail(req.getEmail());
        en.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        en.setTelephone(req.getTelephone()); en.setBio(req.getBio());
        en.setRole(Role.ENSEIGNANT); en.setSpecialite(req.getSpecialite());
        return toResponse(enseignantRepository.save(en));
    }

    // ══════════════════════════════════════════════
    // CREATE MODÉRATEUR
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse creerModerateur(ModerateurRequest req) {
        verifierEmailUnique(req.getEmail());
        verifierTelephoneUnique(req.getTelephone());

        Moderateur m = new Moderateur();
        m.setNom(req.getNom()); m.setPrenom(req.getPrenom());
        m.setEmail(req.getEmail());
        m.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        m.setTelephone(req.getTelephone()); m.setBio(req.getBio());
        m.setRole(Role.MODERATEUR); m.setType(req.getType());
        return toResponse(moderateurRepository.save(m));
    }

    // ══════════════════════════════════════════════
    // UPDATE ÉTUDIANT
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse modifierEtudiant(Long id, EtudiantRequest req) {
        Etudiant e = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable : " + id));

        if (!e.getEmail().equals(req.getEmail())) verifierEmailUnique(req.getEmail());
        if (!e.getTelephone().equals(req.getTelephone())) verifierTelephoneUnique(req.getTelephone());

        e.setNom(req.getNom()); e.setPrenom(req.getPrenom());
        e.setEmail(req.getEmail()); e.setTelephone(req.getTelephone());
        e.setBio(req.getBio());
        if (req.getMotDePasse() != null && !req.getMotDePasse().isBlank())
            e.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(etudiantRepository.save(e));
    }

    // ══════════════════════════════════════════════
    // UPDATE ENSEIGNANT
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse modifierEnseignant(Long id, EnseignantRequest req) {
        Enseignant en = enseignantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable : " + id));

        if (!en.getEmail().equals(req.getEmail())) verifierEmailUnique(req.getEmail());
        if (!en.getTelephone().equals(req.getTelephone())) verifierTelephoneUnique(req.getTelephone());

        en.setNom(req.getNom()); en.setPrenom(req.getPrenom());
        en.setEmail(req.getEmail()); en.setTelephone(req.getTelephone());
        en.setBio(req.getBio()); en.setSpecialite(req.getSpecialite());
        if (req.getMotDePasse() != null && !req.getMotDePasse().isBlank())
            en.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(enseignantRepository.save(en));
    }

    // ══════════════════════════════════════════════
    // UPDATE MODÉRATEUR
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse modifierModerateur(Long id, ModerateurRequest req) {
        Moderateur m = moderateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modérateur introuvable : " + id));

        if (!m.getEmail().equals(req.getEmail())) verifierEmailUnique(req.getEmail());
        if (!m.getTelephone().equals(req.getTelephone())) verifierTelephoneUnique(req.getTelephone());

        m.setNom(req.getNom()); m.setPrenom(req.getPrenom());
        m.setEmail(req.getEmail()); m.setTelephone(req.getTelephone());
        m.setBio(req.getBio()); m.setType(req.getType());
        if (req.getMotDePasse() != null && !req.getMotDePasse().isBlank())
            m.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(moderateurRepository.save(m));
    }

    // ══════════════════════════════════════════════
    // UPLOAD PHOTO (Admin peut le faire pour n'importe qui)
    // ══════════════════════════════════════════════

    @Transactional
    public UtilisateurResponse uploadPhoto(Long id, MultipartFile fichier) {
        Utilisateur utilisateur = findUtilisateur(id);

        // Supprimer l'ancienne photo si existante
        fileStorageService.supprimerPhoto(utilisateur.getPhoto());

        // Sauvegarder la nouvelle
        String chemin = fileStorageService.sauvegarderPhoto(fichier, id);
        utilisateur.setPhoto(chemin);

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    // ══════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════

    @Transactional
    public void supprimerUtilisateur(Long id) {
        Utilisateur utilisateur = findUtilisateur(id);
        fileStorageService.supprimerPhoto(utilisateur.getPhoto());
        utilisateurRepository.deleteById(id);
    }

    // ══════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════

    private Utilisateur findUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));
    }

    private void verifierEmailUnique(String email) {
        if (utilisateurRepository.existsByEmail(email))
            throw new RuntimeException("Email déjà utilisé : " + email);
    }

    private void verifierTelephoneUnique(String telephone) {
        if (utilisateurRepository.existsByTelephone(telephone))
            throw new RuntimeException("Téléphone déjà utilisé : " + telephone);
    }

    public UtilisateurResponse toResponse(Utilisateur u) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setPrenom(u.getPrenom());
        r.setEmail(u.getEmail());
        r.setTelephone(u.getTelephone());
        r.setBio(u.getBio());
        r.setPhoto(u.getPhoto());
        r.setRole(u.getRole().name());
        if (u instanceof Enseignant en) r.setSpecialite(en.getSpecialite());
        if (u instanceof Moderateur m && m.getType() != null) r.setType(m.getType().name());
        return r;
    }
    
    
    
}
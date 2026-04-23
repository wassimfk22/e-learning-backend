package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ══════════════════════════════════════════
    // READ — liste globale de tous les utilisateurs
    // ══════════════════════════════════════════
    public List<UtilisateurResponse> getTousUtilisateurs() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getTousEtudiants() {
        return etudiantRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getTousEnseignants() {
        return enseignantRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getTousModerateurs() {
        return moderateurRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UtilisateurResponse getUtilisateurById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));
        return toResponse(u);
    }

    // ══════════════════════════════════════════
    // CREATE ÉTUDIANT
    // ══════════════════════════════════════════
    @Transactional
    public UtilisateurResponse creerEtudiant(EtudiantRequest req) {
        verifierEmailUnique(req.getEmail());
        Etudiant e = new Etudiant();
        e.setNom(req.getNom()); e.setPrenom(req.getPrenom());
        e.setEmail(req.getEmail());
        e.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        e.setTelephone(req.getTelephone()); e.setPhoto(req.getPhoto()); e.setBio(req.getBio());
        e.setRole(Role.ETUDIANT); e.setDateInscription(new Date());
        return toResponse(etudiantRepository.save(e));
    }

    // ══════════════════════════════════════════
    // CREATE ENSEIGNANT
    // ══════════════════════════════════════════
    @Transactional
    public UtilisateurResponse creerEnseignant(EnseignantRequest req) {
        verifierEmailUnique(req.getEmail());
        Enseignant en = new Enseignant();
        en.setNom(req.getNom()); en.setPrenom(req.getPrenom());
        en.setEmail(req.getEmail());
        en.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        en.setTelephone(req.getTelephone()); en.setPhoto(req.getPhoto()); en.setBio(req.getBio());
        en.setRole(Role.ENSEIGNANT); en.setSpecialite(req.getSpecialite());
        return toResponse(enseignantRepository.save(en));
    }

    // ══════════════════════════════════════════
    // CREATE MODÉRATEUR
    // ══════════════════════════════════════════
    @Transactional
    public UtilisateurResponse creerModerateur(ModerateurRequest req) {
        verifierEmailUnique(req.getEmail());
        Moderateur m = new Moderateur();
        m.setNom(req.getNom()); m.setPrenom(req.getPrenom());
        m.setEmail(req.getEmail());
        m.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        m.setTelephone(req.getTelephone()); m.setPhoto(req.getPhoto()); m.setBio(req.getBio());
        m.setRole(Role.MODERATEUR); m.setType(req.getType());
        return toResponse(moderateurRepository.save(m));
    }

    // ══════════════════════════════════════════
    // UPDATE ÉTUDIANT
    // ══════════════════════════════════════════
    @Transactional
    public UtilisateurResponse modifierEtudiant(Long id, EtudiantRequest req) {
        Etudiant e = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable : " + id));
        if (!e.getEmail().equals(req.getEmail())) verifierEmailUnique(req.getEmail());
        e.setNom(req.getNom()); e.setPrenom(req.getPrenom()); e.setEmail(req.getEmail());
        e.setTelephone(req.getTelephone()); e.setPhoto(req.getPhoto()); e.setBio(req.getBio());
        if (req.getMotDePasse() != null && !req.getMotDePasse().isBlank())
            e.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(etudiantRepository.save(e));
    }

    // ══════════════════════════════════════════
    // UPDATE ENSEIGNANT
    // ══════════════════════════════════════════
    @Transactional
    public UtilisateurResponse modifierEnseignant(Long id, EnseignantRequest req) {
        Enseignant en = enseignantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable : " + id));
        if (!en.getEmail().equals(req.getEmail())) verifierEmailUnique(req.getEmail());
        en.setNom(req.getNom()); en.setPrenom(req.getPrenom()); en.setEmail(req.getEmail());
        en.setTelephone(req.getTelephone()); en.setPhoto(req.getPhoto()); en.setBio(req.getBio());
        en.setSpecialite(req.getSpecialite());
        if (req.getMotDePasse() != null && !req.getMotDePasse().isBlank())
            en.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(enseignantRepository.save(en));
    }

    // ══════════════════════════════════════════
    // UPDATE MODÉRATEUR
    // ══════════════════════════════════════════
    @Transactional
    public UtilisateurResponse modifierModerateur(Long id, ModerateurRequest req) {
        Moderateur m = moderateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modérateur introuvable : " + id));
        if (!m.getEmail().equals(req.getEmail())) verifierEmailUnique(req.getEmail());
        m.setNom(req.getNom()); m.setPrenom(req.getPrenom()); m.setEmail(req.getEmail());
        m.setTelephone(req.getTelephone()); m.setPhoto(req.getPhoto()); m.setBio(req.getBio());
        m.setType(req.getType());
        if (req.getMotDePasse() != null && !req.getMotDePasse().isBlank())
            m.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        return toResponse(moderateurRepository.save(m));
    }

    // ══════════════════════════════════════════
    // DELETE (par id, tous types)
    // ══════════════════════════════════════════
    @Transactional
    public void supprimerUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id))
            throw new RuntimeException("Utilisateur introuvable : " + id);
        utilisateurRepository.deleteById(id);
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════
    private void verifierEmailUnique(String email) {
        if (utilisateurRepository.existsByEmail(email))
            throw new RuntimeException("Email déjà utilisé : " + email);
    }

    public UtilisateurResponse toResponse(Utilisateur u) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setPrenom(u.getPrenom());
        r.setEmail(u.getEmail());
        r.setTelephone(u.getTelephone());
        r.setPhoto(u.getPhoto());
        r.setBio(u.getBio());
        r.setRole(u.getRole().name());
        if (u instanceof Enseignant en) r.setSpecialite(en.getSpecialite());
        if (u instanceof Moderateur m && m.getType() != null) r.setType(m.getType().name());
        return r;
    }
    
    
    
}
package com.school.elearning.service;

import com.school.elearning.dto.EtudiantRequest;
import com.school.elearning.dto.UtilisateurResponse;
import com.school.elearning.model.Etudiant;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.EtudiantRepository;
import com.school.elearning.repository.UtilisateurRepository;
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
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    // ── GET ALL ────────────────────────────────────────────────────
    public List<UtilisateurResponse> getTousEtudiants() {
        return etudiantRepository.findAll()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET ONE ────────────────────────────────────────────────────
    public UtilisateurResponse getEtudiantById(Long id) {
        return toResponse(findEtudiant(id));
    }

    // ── CREATE ─────────────────────────────────────────────────────
    @Transactional
    public UtilisateurResponse creerEtudiant(EtudiantRequest request) {
        verifierEmailUnique(request.getEmail());
        verifierTelephoneUnique(request.getTelephone());

        Etudiant etudiant = new Etudiant();
        etudiant.setNom(request.getNom());
        etudiant.setPrenom(request.getPrenom());
        etudiant.setEmail(request.getEmail());
        etudiant.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        etudiant.setTelephone(request.getTelephone());
        etudiant.setBio(request.getBio());
        etudiant.setRole(Role.ETUDIANT);
        etudiant.setDateInscription(new Date());

        return toResponse(etudiantRepository.save(etudiant));
    }

    // ── UPDATE ─────────────────────────────────────────────────────
    @Transactional
    public UtilisateurResponse modifierEtudiant(Long id, EtudiantRequest request) {
        Etudiant etudiant = findEtudiant(id);

        if (!etudiant.getEmail().equals(request.getEmail()))
            verifierEmailUnique(request.getEmail());
        if (!etudiant.getTelephone().equals(request.getTelephone()))
            verifierTelephoneUnique(request.getTelephone());

        etudiant.setNom(request.getNom());
        etudiant.setPrenom(request.getPrenom());
        etudiant.setEmail(request.getEmail());
        etudiant.setTelephone(request.getTelephone());
        etudiant.setBio(request.getBio());

        // Mot de passe : uniquement si fourni
        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank())
            etudiant.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));

        return toResponse(etudiantRepository.save(etudiant));
    }

    // ── UPLOAD PHOTO ───────────────────────────────────────────────
    @Transactional
    public UtilisateurResponse uploadPhoto(Long id, MultipartFile fichier) {
        Etudiant etudiant = findEtudiant(id);

        // Supprimer l'ancienne photo si elle existe
        fileStorageService.supprimerPhoto(etudiant.getPhoto());

        // Sauvegarder la nouvelle et stocker le chemin
        String chemin = fileStorageService.sauvegarderPhoto(fichier, id);
        etudiant.setPhoto(chemin);

        return toResponse(etudiantRepository.save(etudiant));
    }

    // ── DELETE ─────────────────────────────────────────────────────
    @Transactional
    public void supprimerEtudiant(Long id) {
        Etudiant etudiant = findEtudiant(id);
        // Supprimer la photo du disque avant de supprimer l'entité
        fileStorageService.supprimerPhoto(etudiant.getPhoto());
        etudiantRepository.deleteById(id);
    }

    // ── HELPERS ────────────────────────────────────────────────────
    private Etudiant findEtudiant(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable : " + id));
    }

    private void verifierEmailUnique(String email) {
        if (utilisateurRepository.existsByEmail(email))
            throw new RuntimeException("Email déjà utilisé : " + email);
    }

    private void verifierTelephoneUnique(String telephone) {
        if (utilisateurRepository.existsByTelephone(telephone))
            throw new RuntimeException("Téléphone déjà utilisé : " + telephone);
    }

    public UtilisateurResponse toResponse(Etudiant e) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(e.getId());
        r.setNom(e.getNom());
        r.setPrenom(e.getPrenom());
        r.setEmail(e.getEmail());
        r.setTelephone(e.getTelephone());
        r.setBio(e.getBio());
        r.setPhoto(e.getPhoto());
        r.setRole(e.getRole().name());
        return r;
    }
}
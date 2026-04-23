package com.school.elearning.service;

import com.school.elearning.dto.EtudiantRequest;
import com.school.elearning.dto.UtilisateurResponse;
import com.school.elearning.model.Etudiant;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;

    // ── READ ALL ───────────────────────────────────────────────────
    public List<UtilisateurResponse> getTousEtudiants() {
        return etudiantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── READ ONE ───────────────────────────────────────────────────
    public UtilisateurResponse getEtudiantById(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable avec l'id : " + id));
        return toResponse(etudiant);
    }

    // ── CREATE ─────────────────────────────────────────────────────
    @Transactional
    public UtilisateurResponse creerEtudiant(EtudiantRequest request) {
        if (etudiantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà : " + request.getEmail());
        } else if (etudiantRepository.existsBytelephone(request.getTelephone())) {
        	throw new RuntimeException("Un utilisateur avec cet numéro de tel existe déjà : " + request.getTelephone());
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setNom(request.getNom());
        etudiant.setPrenom(request.getPrenom());
        etudiant.setEmail(request.getEmail());
        etudiant.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        etudiant.setTelephone(request.getTelephone());
        etudiant.setPhoto(request.getPhoto());
        etudiant.setBio(request.getBio());
        etudiant.setRole(Role.ETUDIANT);
        etudiant.setDateInscription(new Date());

        return toResponse(etudiantRepository.save(etudiant));
    }

    // ── UPDATE ─────────────────────────────────────────────────────
    @Transactional
    public UtilisateurResponse modifierEtudiant(Long id, EtudiantRequest request) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable avec l'id : " + id));

        // Vérifier email unique si changé
        if (!etudiant.getEmail().equals(request.getEmail())
                && etudiantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé : " + request.getEmail());
        } else if (etudiantRepository.existsBytelephone(request.getTelephone())) {
        	throw new RuntimeException("Un utilisateur avec cet numéro de tel existe déjà : " + request.getTelephone());
        }

        etudiant.setNom(request.getNom());
        etudiant.setPrenom(request.getPrenom());
        etudiant.setEmail(request.getEmail());
        etudiant.setTelephone(request.getTelephone());
        etudiant.setPhoto(request.getPhoto());
        etudiant.setBio(request.getBio());

        // Mot de passe : seulement si fourni
        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            etudiant.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }

        return toResponse(etudiantRepository.save(etudiant));
    }

    // ── DELETE ─────────────────────────────────────────────────────
    @Transactional
    public void supprimerEtudiant(Long id) {
        if (!etudiantRepository.existsById(id)) {
            throw new RuntimeException("Étudiant introuvable avec l'id : " + id);
        }
        etudiantRepository.deleteById(id);
    }

    // ── MAPPER ─────────────────────────────────────────────────────
    private UtilisateurResponse toResponse(Etudiant e) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(e.getId());
        r.setNom(e.getNom());
        r.setPrenom(e.getPrenom());
        r.setEmail(e.getEmail());
        r.setTelephone(e.getTelephone());
        r.setPhoto(e.getPhoto());
        r.setBio(e.getBio());
        r.setRole(e.getRole().name());
        return r;
    }
}
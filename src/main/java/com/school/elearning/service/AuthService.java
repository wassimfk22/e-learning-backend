package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.*;
import com.school.elearning.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final ModerateurRepository moderateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));
        String token = jwtTokenProvider.generateToken(auth);
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Utilisateur user = userDetails.getUtilisateur();
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getNom(), user.getPrenom());
    }

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Utilisateur user;
        switch (request.getRole()) {
            case ETUDIANT -> {
                Etudiant e = new Etudiant();
                e.setDateInscription(new Date());
                user = e;
            }
            case ENSEIGNANT -> {
                Enseignant e = new Enseignant();
                e.setSpecialite(request.getSpecialite());
                user = e;
            }
            case MODERATEUR -> {
                user = new Moderateur();
            }
            default -> throw new RuntimeException("Rôle non autorisé pour l'inscription");
        }

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        user.setTelephone(request.getTelephone());
        user.setRole(request.getRole());

        utilisateurRepository.save(user);

        // Auto-create BoiteReception
        BoiteReception boite = new BoiteReception();
        boite.setDateCreation(new Date());
        boite.setUtilisateur(user);
        user.setBoiteReception(boite);
        utilisateurRepository.save(user);

        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));
        String token = jwtTokenProvider.generateToken(auth);
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getNom(), user.getPrenom());
    }
}

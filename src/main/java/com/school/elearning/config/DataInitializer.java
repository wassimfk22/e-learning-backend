package com.school.elearning.config;

import com.school.elearning.model.*;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.admin.nom}") private String adminNom;
    @Value("${app.admin.prenom}") private String adminPrenom;

    @Override
    public void run(String... args) {
        if (!utilisateurRepository.existsByEmail(adminEmail)) {
            Administrateur admin = new Administrateur();
            admin.setNom(adminNom);
            admin.setPrenom(adminPrenom);
            admin.setEmail(adminEmail);
            admin.setMotDePasse(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);

            administrateurRepository.save(admin);

            // Create BoiteReception for admin
            BoiteReception boite = new BoiteReception();
            boite.setDateCreation(new Date());
            boite.setUtilisateur(admin);
            admin.setBoiteReception(boite);
            administrateurRepository.save(admin);

            log.info("=== Admin créé avec succès ===");
            log.info("Email: {}", adminEmail);
            log.info("Mot de passe: {}", adminPassword);
        } else {
            log.info("=== Admin existe déjà ===");
        }
    }
}

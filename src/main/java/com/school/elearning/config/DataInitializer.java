package com.school.elearning.config;

import com.school.elearning.model.*;
import com.school.elearning.model.enums.Role;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final EtudiantRepository etudiantRepository;
    private final ModerateurRepository moderateurRepository;
    private final EnseignantRepository enseignantRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createAdmin();
        createEtudiant();
        createModerateur();
        createEnseignant();
    }

    // ================= ADMIN =================
    private void createAdmin() {
        String email = "admin@gmail.com";

        if (!utilisateurRepository.existsByEmail(email)) {
            Administrateur admin = new Administrateur();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setEmail(email);
            admin.setMotDePasse(passwordEncoder.encode("123456"));
            admin.setRole(Role.ADMIN);

            BoiteReception boite = new BoiteReception();
            boite.setDateCreation(new Date());
            boite.setUtilisateur(admin);
            admin.setBoiteReception(boite);

            administrateurRepository.save(admin);

            log.info("=== ADMIN créé ===");
        }
    }

    // ================= ETUDIANT =================
    private void createEtudiant() {
        String email = "etudiant@gmail.com";

        if (!utilisateurRepository.existsByEmail(email)) {
            Etudiant e = new Etudiant();
            e.setNom("Etudiant");
            e.setPrenom("Test");
            e.setEmail(email);
            e.setMotDePasse(passwordEncoder.encode("123456"));
            e.setRole(Role.ETUDIANT);

            etudiantRepository.save(e);

            log.info("=== ETUDIANT créé ===");
        }
    }

    // ================= MODERATEUR =================
    private void createModerateur() {
        String email = "moderateur@gmail.com";

        if (!utilisateurRepository.existsByEmail(email)) {
            Moderateur m = new Moderateur();
            m.setNom("Moderateur");
            m.setPrenom("Test");
            m.setEmail(email);
            m.setMotDePasse(passwordEncoder.encode("123456"));
            m.setRole(Role.MODERATEUR);

            moderateurRepository.save(m);

            log.info("=== MODERATEUR créé ===");
        }
    }

    // ================= ENSEIGNANT =================
    private void createEnseignant() {
        String email = "enseignant@gmail.com";

        if (!utilisateurRepository.existsByEmail(email)) {
            Enseignant e = new Enseignant();
            e.setNom("Prof");
            e.setPrenom("Test");
            e.setEmail(email);
            e.setMotDePasse(passwordEncoder.encode("123456"));
            e.setRole(Role.ENSEIGNANT);

            enseignantRepository.save(e);

            log.info("=== ENSEIGNANT créé ===");
        }
    }
}
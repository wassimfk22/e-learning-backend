package com.school.elearning.controller;

import com.school.elearning.dto.RegisterRequest;
import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import com.school.elearning.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UtilisateurRepository utilisateurRepository;
    private final ModerateurRepository moderateurRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<Utilisateur>> getAllUsers() {
        return ResponseEntity.ok(utilisateurRepository.findAll());
    }

    @PostMapping("/moderateurs")
    public ResponseEntity<Moderateur> createModerateur(@RequestBody RegisterRequest request) {
        Moderateur m = new Moderateur();
        m.setNom(request.getNom());
        m.setPrenom(request.getPrenom());
        m.setEmail(request.getEmail());
        m.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        m.setRole(Role.MODERATEUR);
        m.setTelephone(request.getTelephone());
        return ResponseEntity.ok(moderateurRepository.save(m));
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

package com.school.elearning.controller;

import com.school.elearning.model.Analytique;
import com.school.elearning.repository.AnalytiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytique")
@RequiredArgsConstructor
public class AnalytiqueController {
    private final AnalytiqueRepository analytiqueRepository;

    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Analytique>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(analytiqueRepository.findByEtudiantId(etudiantId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<List<Analytique>> getAll() {
        return ResponseEntity.ok(analytiqueRepository.findAll());
    }
}

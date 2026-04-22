package com.school.elearning.controller;

import com.school.elearning.model.Resultat;
import com.school.elearning.repository.ResultatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resultats")
@RequiredArgsConstructor
public class ResultatController {
    private final ResultatRepository resultatRepository;

    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Resultat>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(resultatRepository.findByEtudiantId(etudiantId));
    }

    @GetMapping("/etudiant/{etudiantId}/module/{moduleId}")
    public ResponseEntity<Resultat> getByEtudiantAndModule(@PathVariable Long etudiantId, @PathVariable Long moduleId) {
        return ResponseEntity.ok(resultatRepository.findByEtudiantIdAndModuleId(etudiantId, moduleId)
                .orElseThrow(() -> new RuntimeException("Resultat non trouvé")));
    }
}

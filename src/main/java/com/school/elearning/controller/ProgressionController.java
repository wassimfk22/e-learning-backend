package com.school.elearning.controller;

import com.school.elearning.model.*;
import com.school.elearning.service.ProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progression")
@RequiredArgsConstructor
public class ProgressionController {
    private final ProgressionService progressionService;

    @GetMapping("/modules/{etudiantId}")
    public ResponseEntity<List<ProgressionModule>> getProgressions(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(progressionService.getProgressionsEtudiant(etudiantId));
    }

    @GetMapping("/enregistrements/{etudiantId}")
    public ResponseEntity<List<Enregistrement>> getEnregistrements(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(progressionService.getEnregistrementsEtudiant(etudiantId));
    }
}

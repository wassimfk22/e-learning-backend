package com.school.elearning.controller;

import com.school.elearning.dto.CreerCoursRequest;
import com.school.elearning.dto.ModifierCoursRequest;
import com.school.elearning.dto.CoursResponse;
import com.school.elearning.service.CoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;

    // GET /api/cours
    @GetMapping
    public ResponseEntity<List<CoursResponse>> getTousCours() {
        return ResponseEntity.ok(coursService.getTousCours());
    }

    // GET /api/cours/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CoursResponse> getCoursById(@PathVariable Long id) {
        return ResponseEntity.ok(coursService.getCoursById(id));
    }

    // GET /api/cours/module/{moduleId}
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<CoursResponse>> getCoursByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(coursService.getCoursByModule(moduleId));
    }

    // POST /api/cours
    @PostMapping
    public ResponseEntity<CoursResponse> creerCours(
            @Valid @RequestBody CreerCoursRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coursService.creerCours(request, auth));
    }

    // PUT /api/cours/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CoursResponse> modifierCours(
            @PathVariable Long id,
            @Valid @RequestBody ModifierCoursRequest request,
            Authentication auth) {
        return ResponseEntity.ok(coursService.modifierCours(id, request, auth));
    }

    // DELETE /api/cours/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCours(
            @PathVariable Long id,
            Authentication auth) {
        coursService.supprimerCours(id, auth);
        return ResponseEntity.noContent().build();
    }
    
    
    
}
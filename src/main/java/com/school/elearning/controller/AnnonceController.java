package com.school.elearning.controller;

import com.school.elearning.model.Annonce;
import com.school.elearning.service.AnnonceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {
    private final AnnonceService annonceService;

    @GetMapping
    public ResponseEntity<List<Annonce>> getAll() { return ResponseEntity.ok(annonceService.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Annonce> getById(@PathVariable Long id) { return ResponseEntity.ok(annonceService.findById(id)); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Annonce> create(@RequestBody Annonce annonce) { return ResponseEntity.ok(annonceService.save(annonce)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { annonceService.delete(id); return ResponseEntity.noContent().build(); }
}

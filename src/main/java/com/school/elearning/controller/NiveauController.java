package com.school.elearning.controller;

import com.school.elearning.model.Niveau;
import com.school.elearning.service.NiveauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
public class NiveauController {
    private final NiveauService niveauService;

    @GetMapping
    public ResponseEntity<List<Niveau>> getAll() { return ResponseEntity.ok(niveauService.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Niveau> getById(@PathVariable Long id) { return ResponseEntity.ok(niveauService.findById(id)); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Niveau> create(@RequestBody Niveau niveau) { return ResponseEntity.ok(niveauService.save(niveau)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Niveau> update(@PathVariable Long id, @RequestBody Niveau niveau) {
        niveau.setId(id);
        return ResponseEntity.ok(niveauService.save(niveau));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { niveauService.delete(id); return ResponseEntity.noContent().build(); }
}

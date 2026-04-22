package com.school.elearning.controller;

import com.school.elearning.model.Module;
import com.school.elearning.service.ModuleService;
import com.school.elearning.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {
    private final ModuleService moduleService;
    private final ModuleRepository moduleRepository;

    @GetMapping
    public ResponseEntity<List<Module>> getAll() { return ResponseEntity.ok(moduleService.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Module> getById(@PathVariable Long id) { return ResponseEntity.ok(moduleService.findById(id)); }

    @GetMapping("/niveau/{niveauId}")
    public ResponseEntity<List<Module>> getByNiveau(@PathVariable Long niveauId) {
        return ResponseEntity.ok(moduleRepository.findByNiveauId(niveauId));
    }

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<Module>> getByEnseignant(@PathVariable Long enseignantId) {
        return ResponseEntity.ok(moduleRepository.findByEnseignantId(enseignantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Module> create(@RequestBody Module module) { return ResponseEntity.ok(moduleService.save(module)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Module> update(@PathVariable Long id, @RequestBody Module module) {
        module.setId(id);
        return ResponseEntity.ok(moduleService.save(module));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { moduleService.delete(id); return ResponseEntity.noContent().build(); }
}

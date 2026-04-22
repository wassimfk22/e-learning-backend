package com.school.elearning.controller;

import com.school.elearning.model.Cours;
import com.school.elearning.service.CoursService;
import com.school.elearning.repository.CoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {
    private final CoursService coursService;
    private final CoursRepository coursRepository;

    @GetMapping
    public ResponseEntity<List<Cours>> getAll() { return ResponseEntity.ok(coursService.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Cours> getById(@PathVariable Long id) { return ResponseEntity.ok(coursService.findById(id)); }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<Cours>> getByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(coursRepository.findByModuleId(moduleId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Cours> create(@RequestBody Cours cours) { return ResponseEntity.ok(coursService.save(cours)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Cours> update(@PathVariable Long id, @RequestBody Cours cours) {
        cours.setId(id);
        return ResponseEntity.ok(coursService.save(cours));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { coursService.delete(id); return ResponseEntity.noContent().build(); }
}

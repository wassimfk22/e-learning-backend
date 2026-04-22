package com.school.elearning.controller;

import com.school.elearning.model.Stream;
import com.school.elearning.service.StreamService;
import com.school.elearning.repository.StreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {
    private final StreamService streamService;
    private final StreamRepository streamRepository;

    @GetMapping
    public ResponseEntity<List<Stream>> getAll() { return ResponseEntity.ok(streamService.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Stream> getById(@PathVariable Long id) { return ResponseEntity.ok(streamService.findById(id)); }

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<Stream>> getByEnseignant(@PathVariable Long enseignantId) {
        return ResponseEntity.ok(streamRepository.findByEnseignantId(enseignantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Stream> create(@RequestBody Stream stream) { return ResponseEntity.ok(streamService.save(stream)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { streamService.delete(id); return ResponseEntity.noContent().build(); }
}

package com.school.elearning.controller;

import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions-pedagogiques")
@RequiredArgsConstructor
public class QuestionPedagogiqueController {
    private final QuestionPedagogiqueRepository questionPedagogiqueRepository;
    private final ReponsePedagogiqueRepository reponsePedagogiqueRepository;

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<QuestionPedagogique>> getByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(questionPedagogiqueRepository.findByCoursId(coursId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<QuestionPedagogique> poser(@RequestBody QuestionPedagogique q) {
        return ResponseEntity.ok(questionPedagogiqueRepository.save(q));
    }

    @PostMapping("/reponses")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ReponsePedagogique> repondre(@RequestBody ReponsePedagogique r) {
        return ResponseEntity.ok(reponsePedagogiqueRepository.save(r));
    }
}

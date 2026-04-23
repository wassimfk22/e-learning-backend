package com.school.elearning.controller;

import com.school.elearning.model.*;
import com.school.elearning.service.MessageService;
import com.school.elearning.repository.CommunauteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communautes")
@RequiredArgsConstructor
public class CommunauteController {
    private final CommunauteRepository communauteRepository;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Communaute>> getAll() { return ResponseEntity.ok(communauteRepository.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Communaute> getById(@PathVariable Long id) {
        return ResponseEntity.ok(communauteRepository.findById(id).orElseThrow(() -> new RuntimeException("Communauté non trouvée")));
    }

//    @GetMapping("/{communauteId}/messages")
//    public ResponseEntity<List<Message>> getChat(@PathVariable Long communauteId) {
//        return ResponseEntity.ok(messageService.getHistoriqueChat(communauteId));
//    }
}

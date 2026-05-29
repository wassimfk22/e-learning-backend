package com.school.elearning.controller;

import com.school.elearning.dto.MessageBoiteResponse;
import com.school.elearning.model.MessageBoite;
import com.school.elearning.repository.BoiteReceptionRepository;
import com.school.elearning.repository.MessageBoiteRepository;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  BOITE DE RÉCEPTION CONTROLLER                               ║
 * ║  Base : /api/boite-reception                                 ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  GET /mes-messages  → messages de ma boite de réception      ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/boite-reception")
@RequiredArgsConstructor
public class BoiteReceptionController {

    private final BoiteReceptionRepository boiteReceptionRepository;
    private final MessageBoiteRepository messageBoiteRepository;

    // GET /api/boite-reception/mes-messages
    @GetMapping("/mes-messages")
    public ResponseEntity<List<MessageBoiteResponse>> getMesMessages(Authentication auth) {
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getUtilisateur().getId();

        return boiteReceptionRepository.findByUtilisateurId(userId)
                .map(boite -> ResponseEntity.ok(
                        messageBoiteRepository
                                .findByBoiteReceptionIdOrderByDateEnvoiDesc(boite.getId())
                                .stream().map(this::toResponse).collect(Collectors.toList())))
                .orElseThrow(() -> new RuntimeException("Boite de réception introuvable"));
    }

    // ── MAPPER ───────────────────────────────────────────────────

    private MessageBoiteResponse toResponse(MessageBoite m) {
        MessageBoiteResponse r = new MessageBoiteResponse();
        r.setId(m.getId());
        r.setContenu(m.getContenu());
        r.setDateEnvoi(m.getDateEnvoi());
        if (m.getExpediteur() != null) {
            r.setExpediteurId(m.getExpediteur().getId());
            r.setExpediteurNom(m.getExpediteur().getNom());
            r.setExpediteurPrenom(m.getExpediteur().getPrenom());
            r.setExpediteurPhoto(m.getExpediteur().getPhoto());
            r.setExpediteurRole(m.getExpediteur().getRole().name());
        }
        return r;
    }
}
package com.school.elearning.controller;

import com.school.elearning.dto.MessageRequest;
import com.school.elearning.dto.MessageResponse;
import com.school.elearning.service.CommunauteMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  COMMUNAUTÉ MESSAGE CONTROLLER                               ║
 * ║  Accès : ETUDIANT uniquement                                 ║
 * ║  Base  : /api/communaute/messages                            ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  GET  /                          → messages de ma communauté ║
 * ║  POST /                          → envoyer message texte     ║
 * ║  POST /fichier                   → envoyer image/PDF         ║
 * ║  PUT  /{id}                      → modifier mon message      ║
 * ║  DELETE /{id}                    → supprimer mon message     ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/communaute/messages")
@PreAuthorize("hasRole('ETUDIANT')")
@RequiredArgsConstructor
public class CommunauteMessageController {

    private final CommunauteMessageService messageService;

    // ── GET TOUS LES MESSAGES ────────────────────────────────────
    // GET /api/communaute/messages
    // Retourne les messages racines + leurs réponses directes
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(Authentication auth) {
        return ResponseEntity.ok(messageService.getMessagesDeMaCommunaute(auth));
    }

    // ── ENVOYER UN MESSAGE TEXTE ─────────────────────────────────
    // POST /api/communaute/messages
    // Body: { "contenu": "Bonjour tout le monde !", "parentId": null }
    // Body (réponse): { "contenu": "Je suis d'accord !", "parentId": 5 }
    @PostMapping
    public ResponseEntity<MessageResponse> envoyerMessage(
            @RequestBody MessageRequest request,
            Authentication auth) {
        return ResponseEntity.ok(messageService.envoyerMessage(request, auth));
    }

    // ── ENVOYER UN FICHIER (IMAGE OU PDF) ────────────────────────
    // POST /api/communaute/messages/fichier
    // Content-Type: multipart/form-data
    // Champ "fichier"  → le fichier (jpg/jpeg/png/webp/pdf)
    // Champ "contenu"  → texte optionnel accompagnant le fichier
    // Champ "parentId" → ID du message parent (optionnel, pour répondre)
    @PostMapping(value = "/fichier", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> envoyerFichier(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam(value = "contenu", required = false) String contenu,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Authentication auth) {
        return ResponseEntity.ok(messageService.envoyerFichier(fichier, contenu, parentId, auth));
    }

    // ── MODIFIER UN MESSAGE ──────────────────────────────────────
    // PUT /api/communaute/messages/{id}
    // Body: { "contenu": "Message corrigé" }
    // ⚠️ Seuls les messages TEXT peuvent être modifiés, par leur auteur uniquement
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> modifierMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(
                messageService.modifierMessage(id, body.get("contenu"), auth));
    }

    // ── SUPPRIMER UN MESSAGE ─────────────────────────────────────
    // DELETE /api/communaute/messages/{id}
    // Si le message a des réponses → soft delete (contenu masqué, structure gardée)
    // Si le message n'a pas de réponses → suppression physique
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerMessage(
            @PathVariable Long id,
            Authentication auth) {
        messageService.supprimerMessage(id, auth);
        return ResponseEntity.ok("Message supprimé avec succès");
    }
    
    
    
}
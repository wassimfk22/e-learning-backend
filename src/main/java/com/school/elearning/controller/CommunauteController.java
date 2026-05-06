package com.school.elearning.controller;

import com.school.elearning.model.*;
import com.school.elearning.service.CommunauteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communautes")
@RequiredArgsConstructor
public class CommunauteController {
    
    private final CommunauteService communauteService;

    // ... tes autres méthodes ...

    @PostMapping("/associer-niveau/{niveauId}")
    public ResponseEntity<Communaute> creerCommunaute(Authentication auth, @PathVariable Long niveauId) {
        return ResponseEntity.ok(communauteService.associerANiveau(auth, niveauId));
    }
    
    
    
}

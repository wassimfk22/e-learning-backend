package com.school.elearning.service;

import com.school.elearning.dto.ModuleRequest;
import com.school.elearning.dto.ModuleResponse;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final NiveauRepository niveauRepository;
    private final EnseignantRepository enseignantRepository;

    // ── READ ALL ────────────────────────────────────────────────
    public List<ModuleResponse> getTousModules() {
        return moduleRepository.findAll()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── READ ONE ────────────────────────────────────────────────
    public ModuleResponse getModuleById(Long id) {
        return toResponse(findModule(id));
    }

    // ── READ BY NIVEAU ──────────────────────────────────────────
    public List<ModuleResponse> getModulesByNiveau(Long niveauId) {
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + niveauId));
        return moduleRepository.findByNiveau(niveau)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    // ── READ BY ENSEIGNANT ──────────────────────────────────────────
    public List<ModuleResponse> getModulesByEnseignant(Long enseignantId) {
    	Enseignant enseignant = enseignantRepository.findById(enseignantId)
    			.orElseThrow(() -> new RuntimeException("Enseignant introuvable : " + enseignantId));
    	return moduleRepository.findByEnseignant(enseignant)
    			.stream().map(this::toResponse)
    			.collect(Collectors.toList());
    }

    // ── READ MES MODULES (enseignant connecté) ──────────────────
    // L'enseignant voit uniquement ses propres modules
    public List<ModuleResponse> getMesModules(Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        return moduleRepository.findByEnseignant(enseignant)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── CREATE (Modérateur/Admin fournit enseignantId et niveauId) ─
    @Transactional
    public ModuleResponse creerModule(ModuleRequest request) {
        verifierTitreUnique(request.getTitre());

        Niveau niveau = niveauRepository.findById(request.getNiveauId())
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + request.getNiveauId()));
        Enseignant enseignant = enseignantRepository.findById(request.getEnseignantId())
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable : " + request.getEnseignantId()));

        Module module = new Module();
        module.setTitre(request.getTitre());
        module.setDescription(request.getDescription());
        module.setDuree(request.getDuree());
        module.setNiveau(niveau);
        module.setEnseignant(enseignant);

        return toResponse(moduleRepository.save(module));
    }

    // ── UPDATE COMPLET — PUT ─────────────────────────────────────
    // Remplace tout l'objet — tous les champs sont requis
    @Transactional
    public ModuleResponse modifierModule(Long id, ModuleRequest request) {
        Module module = findModule(id);

        // Vérifier titre unique seulement si changé
        if (!module.getTitre().equals(request.getTitre())) {
            verifierTitreUnique(request.getTitre());
        }

        Niveau niveau = niveauRepository.findById(request.getNiveauId())
                .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + request.getNiveauId()));
        Enseignant enseignant = enseignantRepository.findById(request.getEnseignantId())
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable : " + request.getEnseignantId()));

        // Remplacement complet
        module.setTitre(request.getTitre());
        module.setDescription(request.getDescription());
        module.setDuree(request.getDuree());
        module.setNiveau(niveau);
        module.setEnseignant(enseignant);

        return toResponse(moduleRepository.save(module));
    }

    // ── UPDATE PARTIEL — PATCH ───────────────────────────────────
    // Modifie uniquement les champs non-null reçus dans la requête
    @Transactional
    public ModuleResponse modifierModulePartiellement(Long id, ModuleRequest request) {
        Module module = findModule(id);

        if (request.getTitre() != null) {
            if (!module.getTitre().equals(request.getTitre())) {
                verifierTitreUnique(request.getTitre());
            }
            module.setTitre(request.getTitre());
        }

        if (request.getDescription() != null) {
            module.setDescription(request.getDescription());
        }

        if (request.getDuree() != null) {
            module.setDuree(request.getDuree());
        }

        if (request.getNiveauId() != null) {
            Niveau niveau = niveauRepository.findById(request.getNiveauId())
                    .orElseThrow(() -> new RuntimeException("Niveau introuvable : " + request.getNiveauId()));
            module.setNiveau(niveau);
        }

        if (request.getEnseignantId() != null) {
            Enseignant enseignant = enseignantRepository.findById(request.getEnseignantId())
                    .orElseThrow(() -> new RuntimeException("Enseignant introuvable : " + request.getEnseignantId()));
            module.setEnseignant(enseignant);
        }

        return toResponse(moduleRepository.save(module));
    }

    // ── DELETE ───────────────────────────────────────────────────
    @Transactional
    public void supprimerModule(Long id) {
        if (!moduleRepository.existsById(id))
            throw new RuntimeException("Module introuvable : " + id);
        moduleRepository.deleteById(id);
    }

    // ── HELPERS ──────────────────────────────────────────────────

    private Module findModule(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module introuvable : " + id));
    }

    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ou pas un enseignant"));
    }

    private void verifierTitreUnique(String titre) {
        if (moduleRepository.existsByTitre(titre))
            throw new RuntimeException("Un module avec ce titre existe déjà : " + titre);
    }

    // ── MAPPER ───────────────────────────────────────────────────
    private ModuleResponse toResponse(Module m) {
        ModuleResponse r = new ModuleResponse();
        r.setId(m.getId());
        r.setTitre(m.getTitre());
        r.setDescription(m.getDescription());
        r.setDuree(m.getDuree());
        if (m.getNiveau() != null) {
            r.setNiveauId(m.getNiveau().getId());
            r.setNiveauNom(m.getNiveau().getNom());
        }
        if (m.getEnseignant() != null) {
            r.setEnseignantId(m.getEnseignant().getId());
            r.setEnseignantNom(m.getEnseignant().getNom());
            r.setEnseignantPrenom(m.getEnseignant().getPrenom());
        }
        return r;
    }
}
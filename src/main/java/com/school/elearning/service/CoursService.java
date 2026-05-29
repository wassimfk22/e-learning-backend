package com.school.elearning.service;

import com.school.elearning.dto.CreerCoursRequest;
import com.school.elearning.dto.ModifierCoursRequest;
import com.school.elearning.dto.CoursResponse;
import com.school.elearning.dto.CoursMapper;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.TypeContent;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursService {

    private final CoursRepository coursRepository;
    private final ModuleRepository moduleRepository;
    private final EnseignantRepository enseignantRepository;
    private final FileTextExtractorService fileTextExtractorService;
    private final GroqCoursService groqCoursService;

    // ───────────────────────────────────────────
    // READ
    // ───────────────────────────────────────────

    public List<CoursResponse> getTousCours() {
        return coursRepository.findAll()
                .stream()
                .map(CoursMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CoursResponse getCoursById(Long id) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException
                		("Cours introuvable : " + id));
        return CoursMapper.toResponse(cours);
    }

    public List<CoursResponse> getCoursByModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));
        return coursRepository.findByModule(module)
                .stream()
                .map(CoursMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ───────────────────────────────────────────
    // CREATE
    // ───────────────────────────────────────────

    @Transactional
    public CoursResponse creerCours(CreerCoursRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        if (!module.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce module ne vous appartient pas");
        }

        Cours cours = new Cours();
        cours.setTitre(request.getTitre());
        cours.setModule(module);
        cours.setDatePublication(new Date());

        List<Content> contents = buildContents(request.getContents(), cours);
        cours.setContents(contents);

        return CoursMapper.toResponse(coursRepository.save(cours));
    }

    // ───────────────────────────────────────────
    // UPDATE
    // ───────────────────────────────────────────

    @Transactional
    public CoursResponse modifierCours(Long id, ModifierCoursRequest request, Authentication auth) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + id));
        Enseignant enseignant = getEnseignantConnecte(auth);

        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce cours ne vous appartient pas");
        }

        // Mettre à jour le titre
        cours.setTitre(request.getTitre());

        // Map des contents existants { id -> Content }
        Map<Long, Content> existants = cours.getContents()
                .stream()
                .collect(Collectors.toMap(Content::getId, c -> c));

        // On vide la liste (orphanRemoval va supprimer ceux qui disparaissent)
        cours.getContents().clear();

        // On reconstruit dans l'ordre envoyé par le prof
        List<ModifierCoursRequest.ContentRequest> contentRequests = request.getContents();
        for (int i = 0; i < contentRequests.size(); i++) {
            ModifierCoursRequest.ContentRequest cr = contentRequests.get(i);

            Content content;
            if (cr.getId() != null && existants.containsKey(cr.getId())) {
                // Contenu existant → on met à jour
                content = existants.get(cr.getId());
            } else {
                // Nouveau contenu → on crée
                content = new Content();
                content.setCours(cours);
            }

            content.setType(cr.getType());
            content.setContent(cr.getContent());
            content.setOrdre(i + 1); // recalcul de l'ordre
            cours.getContents().add(content);
        }

        return CoursMapper.toResponse(coursRepository.save(cours));
    }

    // ───────────────────────────────────────────
    // DELETE
    // ───────────────────────────────────────────

    @Transactional
    public void supprimerCours(Long id, Authentication auth) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + id));
        Enseignant enseignant = getEnseignantConnecte(auth);

        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce cours ne vous appartient pas");
        }

        // Les contents sont supprimés automatiquement via cascade + orphanRemoval
        coursRepository.delete(cours);
    }

    // ───────────────────────────────────────────
    // HELPERS PRIVÉS
    // ───────────────────────────────────────────

    private List<Content> buildContents(List<CreerCoursRequest.ContentRequest> requests, Cours cours) {
        List<Content> contents = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            CreerCoursRequest.ContentRequest cr = requests.get(i);
            Content content = new Content();
            content.setType(cr.getType());
            content.setContent(cr.getContent());
            content.setOrdre(i + 1);
            content.setCours(cours);
            contents.add(content);
        }
        return contents;
    }

    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un enseignant"));
    }
    
    @Transactional
    public CoursResponse creerCoursDepuisFichier(String titre, Long moduleId,
                                                  MultipartFile fichier,
                                                  Authentication auth) throws IOException {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        if (!module.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Ce module ne vous appartient pas");
        }

        String texteExtrait = fileTextExtractorService.extraireTexte(fichier);
        if (texteExtrait == null || texteExtrait.isBlank()) {
            throw new RuntimeException("Le fichier est vide ou illisible");
        }

        String contenuGenere = groqCoursService.genererCoursDepuisTexte(texteExtrait);

        Cours cours = new Cours();
        cours.setTitre(titre);
        cours.setModule(module);
        cours.setDatePublication(new Date());

        Content content = new Content();
        content.setType(TypeContent.TEXT);
        content.setContent(contenuGenere);
        content.setOrdre(1);
        content.setCours(cours);

        cours.setContents(List.of(content));
        return CoursMapper.toResponse(coursRepository.save(cours));
    }
    
}
// ════════════════════════════════════════════════════
// QuestionPedagogiqueService.java
// ════════════════════════════════════════════════════
package com.school.elearning.service;
 
import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class QuestionPedagogiqueService {
 
    private final QuestionPedagogiqueRepository questionRepo;
    private final CoursRepository coursRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
 
    /** Étudiant pose une question */
    @Transactional
    public QuestionPedagogique poserQuestion(String enonce, Long coursId, Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Etudiant etudiant = etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));
 
        QuestionPedagogique question = new QuestionPedagogique();
        question.setEnonce(enonce);
        question.setDatePost(new Date());
        question.setEtudiant(etudiant);
        question.setCours(cours);
        return questionRepo.save(question);
    }
 
    /** Lister les questions d'un cours — pour l'enseignant */
    public List<QuestionPedagogique> getQuestionsByCours(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));
        return questionRepo.findByCours(cours);
    }
}
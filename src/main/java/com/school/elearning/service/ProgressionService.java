package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressionService {
    private final ProgressionModuleRepository progressionModuleRepository;
    private final EnregistrementRepository enregistrementRepository;

    public ProgressionModule inscrireModule(Etudiant etudiant, Module module) {
        ProgressionModule p = new ProgressionModule();
        p.setEtudiant(etudiant);
        p.setModule(module);
        p.setDateInscription(new Date());
        p.setStatut(StatutProgression.NON_COMMENCE);
        p.setPourcentageCompletude(0);
        return progressionModuleRepository.save(p);
    }

    public Enregistrement enregistrerCours(Etudiant etudiant, Cours cours) {
        Enregistrement e = new Enregistrement();
        e.setEtudiant(etudiant);
        e.setCours(cours);
        e.setDateEnregistrement(new Date());
        e.setStatut(StatutEnregistrement.A_VOIR);
        return enregistrementRepository.save(e);
    }

    public List<ProgressionModule> getProgressionsEtudiant(Long etudiantId) {
        return progressionModuleRepository.findByEtudiantId(etudiantId);
    }

    public List<Enregistrement> getEnregistrementsEtudiant(Long etudiantId) {
        return enregistrementRepository.findByEtudiantId(etudiantId);
    }
}

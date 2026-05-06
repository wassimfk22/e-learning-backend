package com.school.elearning.service;

import com.school.elearning.model.Evenement;
import com.school.elearning.repository.EvenementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvenementService {
    private final EvenementRepository evenementRepository;

    public List<Evenement> findAll() { return evenementRepository.findAll(); }
    public Evenement findById(Long id) { return evenementRepository.findById(id).orElseThrow(() -> new RuntimeException("Evenement non trouvé")); }
    public Evenement save(Evenement entity) { return evenementRepository.save(entity); }
    public void delete(Long id) { evenementRepository.deleteById(id); }
}

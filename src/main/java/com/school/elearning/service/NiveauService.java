package com.school.elearning.service;

import com.school.elearning.model.Niveau;
import com.school.elearning.repository.NiveauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NiveauService {
    private final NiveauRepository niveauRepository;

    public List<Niveau> findAll() { return niveauRepository.findAll(); }
    public Niveau findById(Long id) { return niveauRepository.findById(id).orElseThrow(() -> new RuntimeException("Niveau non trouvé")); }
    public Niveau save(Niveau entity) { return niveauRepository.save(entity); }
    public void delete(Long id) { niveauRepository.deleteById(id); }
}

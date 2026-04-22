package com.school.elearning.service;

import com.school.elearning.model.Annonce;
import com.school.elearning.repository.AnnonceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnonceService {
    private final AnnonceRepository annonceRepository;

    public List<Annonce> findAll() { return annonceRepository.findAll(); }
    public Annonce findById(Long id) { return annonceRepository.findById(id).orElseThrow(() -> new RuntimeException("Annonce non trouvé")); }
    public Annonce save(Annonce entity) { return annonceRepository.save(entity); }
    public void delete(Long id) { annonceRepository.deleteById(id); }
}

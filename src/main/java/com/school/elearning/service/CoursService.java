package com.school.elearning.service;

import com.school.elearning.model.Cours;
import com.school.elearning.repository.CoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursService {
    private final CoursRepository coursRepository;

    public List<Cours> findAll() { return coursRepository.findAll(); }
    public Cours findById(Long id) { return coursRepository.findById(id).orElseThrow(() -> new RuntimeException("Cours non trouvé")); }
    public Cours save(Cours entity) { return coursRepository.save(entity); }
    public void delete(Long id) { coursRepository.deleteById(id); }
}

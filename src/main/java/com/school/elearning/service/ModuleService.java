package com.school.elearning.service;

import com.school.elearning.model.Module;
import com.school.elearning.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {
    private final ModuleRepository moduleRepository;

    public List<Module> findAll() { return moduleRepository.findAll(); }
    public Module findById(Long id) { return moduleRepository.findById(id).orElseThrow(() -> new RuntimeException("Module non trouvé")); }
    public Module save(Module entity) { return moduleRepository.save(entity); }
    public void delete(Long id) { moduleRepository.deleteById(id); }
}

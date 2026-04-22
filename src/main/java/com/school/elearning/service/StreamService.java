package com.school.elearning.service;

import com.school.elearning.model.Stream;
import com.school.elearning.repository.StreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamService {
    private final StreamRepository streamRepository;

    public List<Stream> findAll() { return streamRepository.findAll(); }
    public Stream findById(Long id) { return streamRepository.findById(id).orElseThrow(() -> new RuntimeException("Stream non trouvé")); }
    public Stream save(Stream entity) { return streamRepository.save(entity); }
    public void delete(Long id) { streamRepository.deleteById(id); }
}

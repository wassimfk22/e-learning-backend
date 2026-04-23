package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final BoiteReceptionRepository boiteReceptionRepository;
    private final CommunauteRepository communauteRepository;
    
    
    

    
}

package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.model.enums.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final QuizRepository quizRepository;
    private final ExamenRepository examenRepository;
    private final QuestionRepository questionRepository;
    private final ReponseEtudiantRepository reponseEtudiantRepository;
    private final NoteRepository noteRepository;
    private final CorrectionRepository correctionRepository;
   
    
    
    
    

}

// ════════════════════════════════════════════════════
// ReponseQuizRepository.java  →  repository/
// ════════════════════════════════════════════════════
package com.school.elearning.repository;
 
import com.school.elearning.model.QuestionQuiz;
import com.school.elearning.model.ReponseQuiz;
import com.school.elearning.model.TentativeQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ReponseQuizRepository extends JpaRepository<ReponseQuiz, Long> {
 
    List<ReponseQuiz> findByTentative(TentativeQuiz tentative);
 
    // Vérifier si l'étudiant a déjà répondu à cette question dans cette tentative
    Optional<ReponseQuiz> findByTentativeAndQuestion(TentativeQuiz tentative, QuestionQuiz question);
 
    int countByTentative(TentativeQuiz tentative);
    
    
    
}
// ════════════════════════════════════════════════════
// QuestionPedagogiqueController.java
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.model.QuestionPedagogique;
import com.school.elearning.service.QuestionPedagogiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionPedagogiqueController {
 
    private final QuestionPedagogiqueService questionService;
 
    // POST /api/questions — Rôle: ETUDIANT
    // Body: { "enonce": "Comment fonctionne @Transactional ?", "coursId": 2 }
    @PostMapping
    public ResponseEntity<QuestionPedagogique> poser(@RequestBody Map<String, Object> body,
                                                      Authentication auth) {
        String enonce = (String) body.get("enonce");
        Long coursId = Long.valueOf(body.get("coursId").toString());
        return ResponseEntity.ok(questionService.poserQuestion(enonce, coursId, auth));
    }
 
    // GET /api/questions/cours/{coursId} — Rôle: ENSEIGNANT
    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<QuestionPedagogique>> getByModule(@PathVariable Long coursId) {
        return ResponseEntity.ok(questionService.getQuestionsByCours(coursId));
    }
}
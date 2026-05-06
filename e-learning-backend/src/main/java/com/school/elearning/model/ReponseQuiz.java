// ════════════════════════════════════════════════════
// ReponseQuiz.java  →  model/
// ════════════════════════════════════════════════════
package com.school.elearning.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(
    name = "reponses_quiz",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"tentative_id", "question_id"},
        name = "uk_tentative_question"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReponseQuiz {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String reponseChoisie;
 
    private boolean estCorrecte;
    private double pointsObtenus;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tentative_id", nullable = false)
    private TentativeQuiz tentative;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionQuiz question;
}
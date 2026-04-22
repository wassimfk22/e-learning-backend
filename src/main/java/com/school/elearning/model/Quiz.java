package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Quiz extends Evaluation {
	
    private int nombreTentativesMax;

    // Chaque quiz appartient à un cours
    @ManyToOne @JoinColumn(name = "cours_id")
    private Cours cours;
}

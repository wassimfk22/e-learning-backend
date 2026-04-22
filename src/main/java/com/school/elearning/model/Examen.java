package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "examens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Examen extends Evaluation {
	
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePassage;

    // Chaque exam contient plusieurs questions
    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
    
    // Chaque examen va avoir une correction
    @OneToOne @JoinColumn (name = "correction_id")
    private Correction correction;
    
    
}

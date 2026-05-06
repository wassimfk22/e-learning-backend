package com.school.elearning.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity @Table(name = "cours")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cours {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titre;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datePublication;

    @ManyToOne @JoinColumn(name = "module_id")
    private Module module;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<Content> contents;

    // Un cours peut contenir une liste des quizs
    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "cours")
    private List<QuestionPedagogique> questionsPedagogiques;
}

package com.school.elearning.model;

import com.school.elearning.model.enums.TypeContent;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "contents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Content {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeContent type;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int ordre;

    @ManyToOne @JoinColumn(name = "cours_id")
    private Cours cours;
}

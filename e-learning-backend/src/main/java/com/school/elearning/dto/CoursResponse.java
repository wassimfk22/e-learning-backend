// CoursResponse.java
package com.school.elearning.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CoursResponse {
    private Long id;
    private String titre;
    private Date datePublication;
    private Long moduleId;
    private String moduleTitre;
    private List<ContentResponse> contents;
}
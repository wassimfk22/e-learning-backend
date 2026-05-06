// ContentResponse.java
package com.school.elearning.dto;

import com.school.elearning.model.enums.TypeContent;
import lombok.Data;

@Data
public class ContentResponse {
    private Long id;
    private TypeContent type;
    private String content;
    private int ordre;
}
package com.cloudbrain.dto.response.image;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AnnotationVO {

    private String annotationId;
    private String imageId;
    private String annotatorId;
    private String annotationType;
    private String coordinates;
    private String label;
    private String measurement;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

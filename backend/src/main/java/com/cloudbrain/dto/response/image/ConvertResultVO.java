package com.cloudbrain.dto.response.image;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ConvertResultVO {

    private String imageId;
    private String sourceFormat;
    private String targetFormat;
    private String outputPath;
    private Long outputSize;
    private Integer status;
    private LocalDateTime createTime;
}

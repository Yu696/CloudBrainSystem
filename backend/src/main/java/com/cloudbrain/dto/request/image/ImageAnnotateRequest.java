package com.cloudbrain.dto.request.image;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageAnnotateRequest {

    @NotBlank(message = "影像ID不能为空")
    private String imageId;

    @NotBlank(message = "标注类型不能为空")
    private String annotationType;

    @NotBlank(message = "标注坐标不能为空")
    private String coordinates;

    private String label;

    private String measurement;

    private String description;
}

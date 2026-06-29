package com.cloudbrain.dto.request.image;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageConvertRequest {

    @NotBlank(message = "影像ID不能为空")
    private String imageId;

    @NotBlank(message = "目标格式不能为空")
    private String targetFormat;
}

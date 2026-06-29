package com.cloudbrain.dto.request.image;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageCompareRequest {

    @NotBlank(message = "第一张影像ID不能为空")
    private String imageId1;

    @NotBlank(message = "第二张影像ID不能为空")
    private String imageId2;
}

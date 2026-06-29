package com.cloudbrain.dto.request.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ImageUploadRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    private String examinationId;

    private String modality;

    @Size(max = 50, message = "身体部位长度不能超过50")
    private String bodyPart;
}

package com.cloudbrain.dto.response.image;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ImageVO {

    private String imageId;
    private String imageName;
    private Integer imageType;
    private String imageTypeName;
    private String modality;
    private String bodyPart;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String examinationId;
    private String patientId;
    private String patientName;
    private String filePath;
    private Integer status;
    private String uploaderId;
    private LocalDateTime uploadTime;
    private LocalDateTime createTime;

    public static ImageVO fromEntity(com.cloudbrain.entity.MedicalImage img) {
        return ImageVO.builder()
                .imageId(img.getImageId())
                .imageName(img.getImageName())
                .imageType(img.getImageType())
                .imageTypeName(mapImageType(img.getImageType()))
                .modality(img.getModality())
                .bodyPart(img.getBodyPart())
                .fileSize(img.getFileSize())
                .width(img.getWidth())
                .height(img.getHeight())
                .examinationId(img.getExaminationId())
                .patientId(img.getPatientId())
                .filePath(img.getFilePath())
                .status(img.getStatus())
                .uploaderId(img.getUploaderId())
                .uploadTime(img.getUploadTime())
                .createTime(img.getCreateTime())
                .build();
    }

    private static String mapImageType(Integer type) {
        return switch (type != null ? type : -1) {
            case 0 -> "DICOM";
            case 1 -> "JPG";
            case 2 -> "PNG";
            case 3 -> "其他";
            default -> "未知";
        };
    }
}

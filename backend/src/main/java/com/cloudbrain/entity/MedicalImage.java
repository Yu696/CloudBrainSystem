package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_image")
public class MedicalImage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("image_id")
    private String imageId;

    @TableField("examination_id")
    private String examinationId;

    @TableField("patient_id")
    private String patientId;

    @TableField("image_name")
    private String imageName;

    @TableField("image_type")
    private Integer imageType;

    @TableField("file_path")
    private String filePath;

    @TableField("file_size")
    private Long fileSize;

    private Integer width;

    private Integer height;

    private String modality;

    @TableField("body_part")
    private String bodyPart;

    @TableField("study_uid")
    private String studyUid;

    @TableField("series_uid")
    private String seriesUid;

    @TableField("upload_time")
    private LocalDateTime uploadTime;

    @TableField("uploader_id")
    private String uploaderId;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

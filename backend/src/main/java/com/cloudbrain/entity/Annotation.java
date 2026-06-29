package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("annotation")
public class Annotation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("annotation_id")
    private String annotationId;

    @TableField("image_id")
    private String imageId;

    @TableField("annotator_id")
    private String annotatorId;

    @TableField("annotation_type")
    private String annotationType;

    private String coordinates;

    private String label;

    private String measurement;

    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

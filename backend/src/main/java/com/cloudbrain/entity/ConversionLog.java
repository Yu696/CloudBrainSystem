package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversion_log")
public class ConversionLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("image_id")
    private String imageId;

    @TableField("source_format")
    private String sourceFormat;

    @TableField("target_format")
    private String targetFormat;

    @TableField("output_path")
    private String outputPath;

    @TableField("output_size")
    private Long outputSize;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

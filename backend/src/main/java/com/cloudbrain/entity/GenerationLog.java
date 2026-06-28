package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 生成日志实体
 */
@Data
@TableName("generation_log")
public class GenerationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("generation_id")
    private String generationId;

    @TableField("target_type")
    private Integer targetType;

    @TableField("target_id")
    private String targetId;

    @TableField("source_text")
    private String sourceText;

    @TableField("generated_content")
    private String generatedContent;

    @TableField("is_confirmed")
    private Integer isConfirmed;

    @TableField("confirmed_by")
    private String confirmedBy;

    @TableField("prompt_template_id")
    private String promptTemplateId;

    @TableField("response_time_ms")
    private Integer responseTimeMs;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

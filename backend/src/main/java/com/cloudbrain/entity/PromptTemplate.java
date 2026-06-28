package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cloudbrain.common.serializer.JsonArrayOrStringDeserializer;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Prompt 模板实体
 */
@Data
@TableName("prompt_template")
public class PromptTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("template_id")
    private String templateId;

    @TableField("template_name")
    private String templateName;

    @TableField("template_type")
    private Integer templateType;

    @TableField("department_id")
    private String departmentId;

    private String content;

    private Integer version;

    @JsonRawValue
    @JsonDeserialize(using = JsonArrayOrStringDeserializer.class)
    private String variables;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

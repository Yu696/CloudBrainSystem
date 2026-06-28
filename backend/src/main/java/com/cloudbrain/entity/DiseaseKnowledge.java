package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cloudbrain.common.serializer.JsonArrayOrStringDeserializer;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 疾病知识库实体
 */
@Data
@TableName("disease_knowledge")
public class DiseaseKnowledge {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("disease_id")
    private String diseaseId;

    @TableField("disease_name")
    private String diseaseName;

    @TableField("icd_code")
    private String icdCode;

    private String category;

    @TableField("related_department_id")
    private String relatedDepartmentId;

    @JsonRawValue
    @JsonDeserialize(using = JsonArrayOrStringDeserializer.class)
    private String symptoms;

    @TableField("risk_factors")
    private String riskFactors;

    @TableField("diagnosis_basis")
    private String diagnosisBasis;

    @TableField("treatment_plan")
    private String treatmentPlan;

    @TableField("similarity_diseases")
    private String similarityDiseases;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

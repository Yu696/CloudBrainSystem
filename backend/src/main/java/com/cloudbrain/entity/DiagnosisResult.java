package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 诊断结果实体
 */
@Data
@TableName("diagnosis_result")
public class DiagnosisResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("diagnosis_id")
    private String diagnosisId;

    @TableField("patient_id")
    private String patientId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("diagnosis_type")
    private Integer diagnosisType;

    @TableField("symptom_data")
    private String symptomData;

    @TableField("disease_matches")
    private String diseaseMatches;

    @TableField("department_recommendations")
    private String departmentRecommendations;

    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    @TableField("analysis_result")
    private String analysisResult;

    private Integer status;

    @TableField("ai_model")
    private String aiModel;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

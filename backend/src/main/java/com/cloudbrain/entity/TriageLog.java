package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能分诊记录实体
 */
@Data
@TableName("triage_log")
public class TriageLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("triage_id")
    private String triageId;

    @TableField("patient_id")
    private String patientId;

    @TableField("chief_complaint")
    private String chiefComplaint;

    @TableField("recommended_department_id")
    private String recommendedDepartmentId;

    @TableField("recommended_department_name")
    private String recommendedDepartmentName;

    @TableField("recommended_doctor_id")
    private String recommendedDoctorId;

    @TableField("recommended_doctor_name")
    private String recommendedDoctorName;

    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    @TableField("analysis_detail")
    private String analysisDetail;

    private Integer status;

    @TableField("ai_model")
    private String aiModel;

    @TableField("response_time_ms")
    private Integer responseTimeMs;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

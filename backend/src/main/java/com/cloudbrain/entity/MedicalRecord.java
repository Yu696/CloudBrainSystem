package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("record_id")
    private String recordId;

    @TableField("patient_id")
    private String patientId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("appointment_id")
    private String appointmentId;

    @TableField("chief_complaint")
    private String chiefComplaint;

    @TableField("present_illness")
    private String presentIllness;

    @TableField("past_history")
    private String pastHistory;

    @TableField("personal_history")
    private String personalHistory;

    @TableField("family_history")
    private String familyHistory;

    @TableField("physical_exam")
    private String physicalExam;

    @TableField("auxiliary_exam")
    private String auxiliaryExam;

    private String diagnosis;

    @TableField("treatment_opinion")
    private String treatmentOpinion;

    private Integer status = 0;

    @TableField("is_ai_generated")
    private Integer isAiGenerated = 0;

    @TableField("ai_generation_id")
    private String aiGenerationId;

    @TableField("diagnosis_time")
    private LocalDateTime diagnosisTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

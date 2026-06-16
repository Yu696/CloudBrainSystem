package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("patient")
public class Patient {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    private String patientId;

    @TableField("user_id")
    private String userId;

    @TableField("medical_record_no")
    private String medicalRecordNo;

    private String name;

    @TableField("id_card")
    private String idCard;

    private Integer gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    private String phone;

    @TableField("emergency_phone")
    private String emergencyPhone;

    private String address;

    @TableField("blood_type")
    private String bloodType;

    @TableField("allergy_history")
    private String allergyHistory;

    @TableField("genetic_diseases")
    private String geneticDiseases;

    @TableField("medical_history")
    private String medicalHistory;

    @TableField("qr_code_url")
    private String qrCodeUrl;

    private Integer source = 1;

    private Integer status = 1;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted = 0;
}

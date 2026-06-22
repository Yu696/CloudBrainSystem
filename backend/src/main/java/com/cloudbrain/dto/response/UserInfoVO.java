package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserInfoVO {
    // 用户基础信息
    private String userId;
    private String userName;
    private String realName;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer userType;
    private String role;

    // 患者档案信息（仅 userType=2 时有值）
    private String patientId;
    private String medicalRecordNo;
    private String name;
    private String idCard;
    private Integer gender;
    private LocalDate birthDate;
    private String emergencyPhone;
    private String address;
    private String bloodType;
    private String allergyHistory;
    private String geneticDiseases;
    private String medicalHistory;
}

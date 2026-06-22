package com.cloudbrain.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    // 用户基础字段
    private String realName;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    private String email;

    // 患者档案字段（仅 userType=2 时有效，可选填）
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

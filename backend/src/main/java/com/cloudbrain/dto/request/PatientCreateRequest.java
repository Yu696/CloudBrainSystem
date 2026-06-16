package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientCreateRequest {

    @NotBlank(message = "患者姓名不能为空")
    private String name;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotNull(message = "出生日期不能为空")
    private LocalDate birthDate;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String emergencyPhone;
    private String address;
    private String bloodType;
    private String allergyHistory;
    private String geneticDiseases;
    private String medicalHistory;
    private Integer source;
}

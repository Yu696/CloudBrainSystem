package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientUpdateRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private String phone;
    private String emergencyPhone;
    private String address;
    private String bloodType;
    private String allergyHistory;
    private String geneticDiseases;
    private String medicalHistory;
    private Integer status;
}

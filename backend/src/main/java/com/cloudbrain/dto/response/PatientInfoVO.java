package com.cloudbrain.dto.response;

import com.cloudbrain.entity.Patient;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PatientInfoVO {

    private String patientId;
    private String userId;
    private String medicalRecordNo;
    private String name;
    private String idCard;
    private Integer gender;
    private LocalDate birthDate;
    private Integer age;
    private String phone;
    private String emergencyPhone;
    private String address;
    private String bloodType;
    private String allergyHistory;
    private String geneticDiseases;
    private String medicalHistory;
    private Integer source;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static PatientInfoVO from(Patient patient) {
        return PatientInfoVO.builder()
                .patientId(patient.getPatientId())
                .userId(patient.getUserId())
                .medicalRecordNo(patient.getMedicalRecordNo())
                .name(patient.getName())
                .idCard(patient.getIdCard())
                .gender(patient.getGender())
                .birthDate(patient.getBirthDate())
                .age(patient.getBirthDate() != null
                        ? LocalDate.now().getYear() - patient.getBirthDate().getYear() : null)
                .phone(patient.getPhone())
                .emergencyPhone(patient.getEmergencyPhone())
                .address(patient.getAddress())
                .bloodType(patient.getBloodType())
                .allergyHistory(patient.getAllergyHistory())
                .geneticDiseases(patient.getGeneticDiseases())
                .medicalHistory(patient.getMedicalHistory())
                .source(patient.getSource())
                .status(patient.getStatus())
                .createTime(patient.getCreateTime())
                .updateTime(patient.getUpdateTime())
                .build();
    }
}

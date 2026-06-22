package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicalRecordCreateRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    private String appointmentId;

    @NotBlank(message = "主诉不能为空")
    private String chiefComplaint;

    private String presentIllness;
    private String pastHistory;
    private String personalHistory;
    private String familyHistory;
    private String physicalExam;
    private String auxiliaryExam;
    private String diagnosis;
    private String treatmentOpinion;
}

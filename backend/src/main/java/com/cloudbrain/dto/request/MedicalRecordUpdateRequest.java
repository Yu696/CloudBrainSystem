package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicalRecordUpdateRequest {

    @NotBlank(message = "病历ID不能为空")
    private String recordId;

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

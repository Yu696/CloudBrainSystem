package com.cloudbrain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatientCreateVO {
    private String patientId;
    private String medicalRecordNo;
}

package com.cloudbrain.dto.response;

import com.cloudbrain.entity.MedicalRecord;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MedicalRecordVO {

    private String recordId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String appointmentId;
    private String chiefComplaint;
    private String presentIllness;
    private String pastHistory;
    private String personalHistory;
    private String familyHistory;
    private String physicalExam;
    private String auxiliaryExam;
    private String diagnosis;
    private String treatmentOpinion;
    private Integer status;
    private Integer isAiGenerated;
    private LocalDateTime diagnosisTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MedicalRecordVO from(MedicalRecord r) {
        return MedicalRecordVO.builder()
                .recordId(r.getRecordId())
                .patientId(r.getPatientId())
                .doctorId(r.getDoctorId())
                .appointmentId(r.getAppointmentId())
                .chiefComplaint(r.getChiefComplaint())
                .presentIllness(r.getPresentIllness())
                .pastHistory(r.getPastHistory())
                .personalHistory(r.getPersonalHistory())
                .familyHistory(r.getFamilyHistory())
                .physicalExam(r.getPhysicalExam())
                .auxiliaryExam(r.getAuxiliaryExam())
                .diagnosis(r.getDiagnosis())
                .treatmentOpinion(r.getTreatmentOpinion())
                .status(r.getStatus())
                .isAiGenerated(r.getIsAiGenerated())
                .diagnosisTime(r.getDiagnosisTime())
                .createTime(r.getCreateTime())
                .updateTime(r.getUpdateTime())
                .build();
    }
}

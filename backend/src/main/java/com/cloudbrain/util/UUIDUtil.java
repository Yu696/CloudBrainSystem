package com.cloudbrain.util;

import java.util.UUID;

public class UUIDUtil {

    public static String generateDeptId() {
        return "DEPT_" + shortUuid();
    }

    public static String generateDoctorId() {
        return "DOC_" + shortUuid();
    }

    public static String generateScheduleId() {
        return "SCH_" + shortUuid();
    }

    public static String generateSlotId() {
        return "SLOT_" + shortUuid();
    }

    public static String generateAppointmentId() {
        return "APT_" + shortUuid();
    }

    public static String generatePaymentId() {
        return "PAY_" + shortUuid();
    }

    public static String generateUserId() {
        return "USR_" + shortUuid();
    }

    public static String generatePatientId() {
        return "PAT_" + shortUuid();
    }

    public static String generateMedicalRecordNo() {
        return "MRN_" + shortUuid();
    }

    public static String generateRecordId() {
        return "REC_" + shortUuid();
    }

    public static String generatePrescriptionId() {
        return "PRS_" + shortUuid();
    }

    public static String generatePrescriptionItemId() {
        return "PRI_" + shortUuid();
    }

    public static String generateExaminationOrderId() {
        return "EXO_" + shortUuid();
    }

    public static String generateExaminationResultId() {
        return "EXR_" + shortUuid();
    }

    public static String generateAuditId() {
        return "AUD_" + shortUuid();
    }

    // ==================== M04 AI 辅助诊断 ====================

    public static String generateTriageId() {
        return "TRI_" + shortUuid();
    }

    public static String generateDiagnosisId() {
        return "DIA_" + shortUuid();
    }

    public static String generateGenerationId() {
        return "GEN_" + shortUuid();
    }

    public static String generateTemplateId() {
        return "PTMP_" + shortUuid();
    }

    public static String generateDiseaseId() {
        return "DK_" + shortUuid();
    }

    // ==================== M05 影像管理 ====================

    public static String generateImageId() {
        return "IMG_" + shortUuid();
    }

    public static String generateAnnotationId() {
        return "ANN_" + shortUuid();
    }

    public static String generateStorageConfigId() {
        return "SC_" + shortUuid();
    }

    // ==================== M07 药库管理 ====================

    public static String generateDrugId() {
        return "DRUG_" + shortUuid();
    }

    public static String generateWarehouseId() {
        return "WH_" + shortUuid();
    }

    public static String generateShipRecordId() {
        return "SHP_" + shortUuid();
    }

    public static String generateStockAlertId() {
        return "SAL_" + shortUuid();
    }

    private static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}

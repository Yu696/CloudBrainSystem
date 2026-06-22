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
    private static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}

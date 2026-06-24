package com.cloudbrain.dto.ai;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * AI 请求中的患者背景信息（所有 AI API 共用）
 * 由后端 PatientContextBuilder 根据 patientId 自动构建
 */
@Data
@Builder
public class PatientContext {

    /** 年龄（从 birthDate 计算） */
    private int age;

    /** 性别：0=女 1=男 */
    private int gender;

    /** 过敏史 */
    private String allergyHistory;

    /** 既往病史 */
    private String medicalHistory;

    /** 遗传病史 */
    private String geneticDiseases;

    /** 血型 */
    private String bloodType;

    /** 当前用药（最近90天） */
    private List<CurrentMedication> currentMedications;

    /** 近期就诊记录（最近5条） */
    private List<RecentRecord> recentRecords;

    // ==================== 内嵌类 ====================

    @Data
    @Builder
    public static class CurrentMedication {
        private String drugName;
        private String dosage;
        private String frequency;
        private String startDate;
    }

    @Data
    @Builder
    public static class RecentRecord {
        private String date;
        private String diagnosis;
        private String departmentName;
        private String doctorName;
    }
}

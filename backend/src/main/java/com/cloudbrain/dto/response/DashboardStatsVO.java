package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsVO {
    // 管理员
    private long todayAppointments;
    private long waitingCount;
    private long monthNewPatients;
    private long todayCompleted;

    // 医生
    private long doctorTodayWaiting;
    private long doctorTodayCompleted;
    private long doctorMonthCompleted;
    private long doctorTotalPatients;

    // 患者
    private long patientTotalAppointments;
    private long patientMonthAppointments;
    private long patientTotalRecords;
    private long patientUnpaidCount;
}

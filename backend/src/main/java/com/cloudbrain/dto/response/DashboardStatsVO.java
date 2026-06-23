package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsVO {
    private long todayAppointments;
    private long waitingCount;
    private long monthNewPatients;
    private long pendingCount;
}

package com.cloudbrain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.dto.response.DashboardStatsVO;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.mapper.AppointmentMapper;
import com.cloudbrain.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentMapper appointmentMapper;
    private final PatientMapper patientMapper;

    public DashboardStatsVO getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();

        long todayAppointments = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getAppointmentDate, today));

        long waitingCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getAppointmentDate, today)
                        .eq(Appointment::getStatus, 1));

        long monthNewPatients = patientMapper.selectCount(
                new LambdaQueryWrapper<Patient>().ge(Patient::getCreateTime, monthStart));

        long pendingCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, 0));

        return DashboardStatsVO.builder()
                .todayAppointments(todayAppointments)
                .waitingCount(waitingCount)
                .monthNewPatients(monthNewPatients)
                .pendingCount(pendingCount)
                .build();
    }
}

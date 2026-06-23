package com.cloudbrain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.dto.response.DashboardStatsVO;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentMapper appointmentMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final UserMapper userMapper;

    public DashboardStatsVO getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();

        // === 管理员统计 ===
        long todayAppointments = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getAppointmentDate, today));

        long waitingCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getAppointmentDate, today)
                        .eq(Appointment::getStatus, 1));

        long monthNewPatients = patientMapper.selectCount(
                new LambdaQueryWrapper<Patient>().ge(Patient::getCreateTime, monthStart));

        long todayCompleted = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getAppointmentDate, today)
                        .eq(Appointment::getStatus, 2));

        // === 获取当前用户信息 ===
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (auth != null && auth.isAuthenticated()) ? (User) auth.getPrincipal() : null;

        String doctorId = null;
        String patientId = null;
        if (currentUser != null && currentUser.getUserType() != null) {
            if (currentUser.getUserType() == 0) {
                Doctor doctor = doctorMapper.selectOne(
                        new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, currentUser.getUserId()));
                if (doctor != null) doctorId = doctor.getDoctorId();
            } else if (currentUser.getUserType() == 2) {
                Patient patient = patientMapper.selectOne(
                        new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, currentUser.getUserId()));
                if (patient != null) patientId = patient.getPatientId();
            }
        }

        // === 医生统计 ===
        long doctorTodayWaiting = 0;
        long doctorTodayCompleted = 0;
        long doctorMonthCompleted = 0;
        long doctorTotalPatients = 0;

        if (doctorId != null) {
            doctorTodayWaiting = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getDoctorId, doctorId)
                            .eq(Appointment::getAppointmentDate, today)
                            .eq(Appointment::getStatus, 1));

            doctorTodayCompleted = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getDoctorId, doctorId)
                            .eq(Appointment::getAppointmentDate, today)
                            .eq(Appointment::getStatus, 2));

            doctorMonthCompleted = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getDoctorId, doctorId)
                            .ge(Appointment::getCreateTime, monthStart)
                            .eq(Appointment::getStatus, 2));

            // 累计接诊患者数（去重）
            doctorTotalPatients = appointmentMapper.selectList(
                    new LambdaQueryWrapper<Appointment>()
                            .select(Appointment::getPatientId)
                            .eq(Appointment::getDoctorId, doctorId)
                            .groupBy(Appointment::getPatientId))
                    .size();
        }

        // === 患者统计 ===
        long patientTotalAppointments = 0;
        long patientMonthAppointments = 0;
        long patientTotalRecords = 0;
        long patientUnpaidCount = 0;

        if (patientId != null) {
            patientTotalAppointments = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>().eq(Appointment::getPatientId, patientId));

            patientMonthAppointments = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getPatientId, patientId)
                            .ge(Appointment::getCreateTime, monthStart));

            patientTotalRecords = medicalRecordMapper.selectCount(
                    new LambdaQueryWrapper<MedicalRecord>().eq(MedicalRecord::getPatientId, patientId));

            patientUnpaidCount = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getPatientId, patientId)
                            .eq(Appointment::getPaymentStatus, 0));
        }

        return DashboardStatsVO.builder()
                .todayAppointments(todayAppointments)
                .waitingCount(waitingCount)
                .monthNewPatients(monthNewPatients)
                .todayCompleted(todayCompleted)
                .doctorTodayWaiting(doctorTodayWaiting)
                .doctorTodayCompleted(doctorTodayCompleted)
                .doctorMonthCompleted(doctorMonthCompleted)
                .doctorTotalPatients(doctorTotalPatients)
                .patientTotalAppointments(patientTotalAppointments)
                .patientMonthAppointments(patientMonthAppointments)
                .patientTotalRecords(patientTotalRecords)
                .patientUnpaidCount(patientUnpaidCount)
                .build();
    }
}

package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.AppointmentBookRequest;
import com.cloudbrain.dto.request.AppointmentCancelRequest;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.Schedule;
import com.cloudbrain.entity.TimeSlot;
import com.cloudbrain.mapper.AppointmentMapper;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.ScheduleMapper;
import com.cloudbrain.mapper.TimeSlotMapper;
import com.cloudbrain.service.appointment.AppointmentService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final TimeSlotMapper timeSlotMapper;
    private final DoctorMapper doctorMapper;
    private final ScheduleMapper scheduleMapper;
    private final PatientMapper patientMapper;

    @Override
    @Transactional
    public Appointment book(AppointmentBookRequest request) {
        // 查找时段
        TimeSlot slot = timeSlotMapper.selectOne(
                new LambdaQueryWrapper<TimeSlot>().eq(TimeSlot::getSlotId, request.getSlotId()));
        if (slot == null) {
            throw new BusinessException("时段不存在");
        }

        // 校验时段状态
        if (slot.getStatus() == 1 && slot.getLockExpiryTime() != null
                && slot.getLockExpiryTime().isBefore(LocalDateTime.now())) {
            // 锁已过期，释放并回补可用时段数
            slot.setStatus(0);
            slot.setLockToken(null);
            slot.setLockedBy(null);
            slot.setLockExpiryTime(null);
            timeSlotMapper.updateById(slot);

            Schedule schedule = scheduleMapper.selectOne(
                    new LambdaQueryWrapper<Schedule>().eq(Schedule::getScheduleId, slot.getScheduleId()));
            if (schedule != null && schedule.getAvailableSlots() != null) {
                schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
                scheduleMapper.updateById(schedule);
            }
        }

        if (slot.getStatus() != 0) {
            throw new BusinessException("该时段不可预约，当前状态：" + slotStatusText(slot.getStatus()));
        }

        // 获取医生信息
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, request.getDoctorId()));
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }

        // 校验医生是否接诊
        if (doctor.getAvailable() != 1) {
            throw new BusinessException("该医生当前不接诊");
        }

        // 校验医生当日预约量是否已满
        long dailyCount = this.count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, request.getDoctorId())
                .eq(Appointment::getAppointmentDate, LocalDate.from(slot.getStartTime()))
                .in(Appointment::getStatus, 0, 1));
        if (dailyCount >= doctor.getMaxDailyPatients()) {
            throw new BusinessException("该医生当日预约已满");
        }

        // 校验患者档案必要信息
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, request.getPatientId()));
        if (patient == null) {
            throw new BusinessException("患者档案不存在");
        }
        if (patient.getName() == null || patient.getName().isEmpty()
                || patient.getIdCard() == null || patient.getIdCard().isEmpty()
                || patient.getPhone() == null || patient.getPhone().isEmpty()) {
            throw new BusinessException("患者档案信息不完整，请先在个人信息中完善姓名、身份证号和手机号");
        }

        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(UUIDUtil.generateAppointmentId());
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setDepartmentId(request.getDepartmentId());
        appointment.setScheduleId(slot.getScheduleId());
        appointment.setTimeSlotId(slot.getSlotId());
        appointment.setAppointmentDate(LocalDate.from(slot.getStartTime()));
        appointment.setTimeSlotDesc(formatTimeSlot(slot));
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setSymptoms(request.getSymptoms());
        appointment.setSource(request.getSource() != null ? request.getSource() : 0);
        appointment.setStatus(0);
        appointment.setPaymentStatus(0);
        appointment.setTotalFee(doctor.getConsultationFee());

        this.save(appointment);

        // 锁定时段
        slot.setStatus(1);
        slot.setLockedBy(request.getPatientId());
        slot.setLockExpiryTime(LocalDateTime.now().plusMinutes(15));
        timeSlotMapper.updateById(slot);

        // 扣减排班可用时段数
        Schedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<Schedule>().eq(Schedule::getScheduleId, slot.getScheduleId()));
        if (schedule != null && schedule.getAvailableSlots() != null && schedule.getAvailableSlots() > 0) {
            schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
            scheduleMapper.updateById(schedule);
        }

        return appointment;
    }

    @Override
    @Transactional
    public void cancel(AppointmentCancelRequest request) {
        Appointment appointment = this.getOne(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentId, request.getAppointmentId()));
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }

        if (appointment.getStatus() != 0 && appointment.getStatus() != 1) {
            throw new BusinessException("当前状态不可取消");
        }

        appointment.setStatus(4);
        appointment.setCancelReason(request.getReason());
        this.updateById(appointment);

        // 释放时段
        if (appointment.getTimeSlotId() != null) {
            TimeSlot slot = timeSlotMapper.selectOne(
                    new LambdaQueryWrapper<TimeSlot>().eq(TimeSlot::getSlotId, appointment.getTimeSlotId()));
            if (slot != null) {
                slot.setStatus(0);
                slot.setAppointmentId(null);
                slot.setLockToken(null);
                slot.setLockedBy(null);
                slot.setLockExpiryTime(null);
                timeSlotMapper.updateById(slot);

                // 恢复排班可用时段数
                Schedule schedule = scheduleMapper.selectOne(
                        new LambdaQueryWrapper<Schedule>().eq(Schedule::getScheduleId, slot.getScheduleId()));
                if (schedule != null && schedule.getAvailableSlots() != null) {
                    schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
                    scheduleMapper.updateById(schedule);
                }
            }
        }
    }

    @Override
    public Appointment getDetail(String appointmentId) {
        Appointment apt = this.getOne(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentId, appointmentId));
        if (apt == null) {
            throw new BusinessException("预约不存在");
        }
        return apt;
    }

    @Override
    public List<Appointment> listByPatient(String patientId) {
        return this.list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId)
                .orderByDesc(Appointment::getCreateTime));
    }

    @Override
    public List<Appointment> listByDoctor(String doctorId) {
        return this.list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, doctorId)
                .orderByDesc(Appointment::getCreateTime));
    }

    private String formatTimeSlot(TimeSlot slot) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return slot.getStartTime().format(fmt) + "-" + slot.getEndTime().format(fmt);
    }

    private String slotStatusText(Integer status) {
        return switch (status) {
            case 0 -> "可用";
            case 1 -> "已锁定";
            case 2 -> "已预约";
            case 3 -> "已完成";
            case 4 -> "已释放";
            default -> "未知";
        };
    }
}

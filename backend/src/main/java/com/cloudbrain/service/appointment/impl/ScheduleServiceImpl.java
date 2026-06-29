package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.ScheduleCreateRequest;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.Schedule;
import com.cloudbrain.entity.TimeSlot;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.ScheduleMapper;
import com.cloudbrain.mapper.TimeSlotMapper;
import com.cloudbrain.service.appointment.ScheduleService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    private final DoctorMapper doctorMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    @Transactional
    public Schedule create(ScheduleCreateRequest request) {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, request.getDoctorId()));
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }

        // 校验时段长度：不能少于5分钟
        int slotDuration = request.getSlotDuration() != null ? request.getSlotDuration() : 30;
        if (slotDuration < 5) {
            throw new BusinessException("时段长度不能少于5分钟");
        }
        long totalMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (slotDuration > totalMinutes) {
            throw new BusinessException("时段长度不能超过班次总时长（" + totalMinutes + "分钟）");
        }

        // 检查是否已有同一天同一班次的排班
        Schedule existing = this.getOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, request.getDoctorId())
                .eq(Schedule::getScheduleDate, request.getScheduleDate())
                .eq(Schedule::getWorkShift, request.getWorkShift()));
        if (existing != null) {
            throw new BusinessException("该医生当天的该班次已有排班");
        }

        int maxPatients = request.getMaxPatients() != null ? request.getMaxPatients() : 20;

        Schedule schedule = new Schedule();
        schedule.setScheduleId(UUIDUtil.generateScheduleId());
        schedule.setDoctorId(request.getDoctorId());
        schedule.setDepartmentId(request.getDepartmentId());
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setWorkShift(request.getWorkShift());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotDuration(slotDuration);
        schedule.setMaxPatients(maxPatients);
        schedule.setStatus(1);
        schedule.setRemark(request.getRemark());

        this.save(schedule);

        // 自动生成时段，availableSlots 用实际生成的时段数
        int slotCount = generateTimeSlots(schedule);
        schedule.setAvailableSlots(slotCount);
        this.updateById(schedule);

        return schedule;
    }

    @Override
    public List<Schedule> query(String doctorId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = startDate.plusDays(7);
        return this.list(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .ge(Schedule::getScheduleDate, startDate)
                .le(Schedule::getScheduleDate, endDate));
    }

    @Override
    public List<TimeSlot> getAvailableSlots(String doctorId, LocalDate date) {
        List<Schedule> schedules = this.list(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getScheduleDate, date));
        if (schedules.isEmpty()) {
            return List.of();
        }
        // 获取当天所有排班的可用时段
        List<String> scheduleIds = schedules.stream()
                .map(Schedule::getScheduleId)
                .toList();
        return timeSlotMapper.selectList(new LambdaQueryWrapper<TimeSlot>()
                .in(TimeSlot::getScheduleId, scheduleIds)
                .eq(TimeSlot::getStatus, 0));
    }

    private int generateTimeSlots(Schedule schedule) {
        LocalTime start = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();
        int duration = schedule.getSlotDuration();
        LocalDate date = schedule.getScheduleDate();

        int count = 0;
        LocalTime current = start;
        while (current.plusMinutes(duration).isBefore(end) || current.plusMinutes(duration).equals(end)) {
            TimeSlot slot = new TimeSlot();
            slot.setSlotId(UUIDUtil.generateSlotId());
            slot.setScheduleId(schedule.getScheduleId());
            slot.setDoctorId(schedule.getDoctorId());
            slot.setStartTime(LocalDateTime.of(date, current));
            slot.setEndTime(LocalDateTime.of(date, current.plusMinutes(duration)));
            slot.setStatus(0);

            timeSlotMapper.insert(slot);

            current = current.plusMinutes(duration);
            count++;
        }
        return count;
    }
}

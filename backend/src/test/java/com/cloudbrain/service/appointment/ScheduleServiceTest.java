package com.cloudbrain.service.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.ScheduleCreateRequest;
import com.cloudbrain.entity.Schedule;
import com.cloudbrain.entity.TimeSlot;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    private static final String DOCTOR_ID = "DOC_001";
    private static final String DEPARTMENT_ID = "DEPT_001";

    @Test
    @Order(1)
    @DisplayName("创建排班 — 正常创建并自动生成时段")
    void testCreateSchedule() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setDoctorId(DOCTOR_ID);
        request.setDepartmentId(DEPARTMENT_ID);
        request.setScheduleDate(LocalDate.now().plusDays(1));
        request.setWorkShift(0);
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(12, 0));
        request.setSlotDuration(30);
        request.setMaxPatients(20);

        Schedule schedule = scheduleService.create(request);

        assertNotNull(schedule);
        assertNotNull(schedule.getScheduleId());
        assertEquals(DOCTOR_ID, schedule.getDoctorId());
        // 8:00-12:00, 30分钟/个, 共8个时段
        assertEquals(8, schedule.getAvailableSlots());

        // 验证时段自动生成
        List<TimeSlot> slots = scheduleService.getAvailableSlots(DOCTOR_ID, LocalDate.now().plusDays(1));
        assertEquals(8, slots.size());
        slots.forEach(s -> assertEquals(0, s.getStatus()));
    }

    @Test
    @Order(2)
    @DisplayName("创建排班 — 同一天同一班次重复创建应抛异常")
    void testCreateDuplicateSchedule() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setDoctorId(DOCTOR_ID);
        request.setDepartmentId(DEPARTMENT_ID);
        request.setScheduleDate(LocalDate.now().plusDays(2));
        request.setWorkShift(1);
        request.setStartTime(LocalTime.of(14, 0));
        request.setEndTime(LocalTime.of(18, 0));

        scheduleService.create(request);

        assertThrows(BusinessException.class, () -> scheduleService.create(request));
    }

    @Test
    @Order(3)
    @DisplayName("查询排班 — 按医生查询一周内排班")
    void testQuerySchedules() {
        scheduleService.create(buildRequest(DOCTOR_ID, LocalDate.now().plusDays(3), 0));

        List<Schedule> schedules = scheduleService.query(DOCTOR_ID, null, null);
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("查询可用时段 — 排班创建后应生成正确的可用时段")
    void testGetAvailableSlots() {
        Schedule schedule = scheduleService.create(buildRequest("DOC_002", LocalDate.now().plusDays(1), 0));

        List<TimeSlot> slots = scheduleService.getAvailableSlots("DOC_002", LocalDate.now().plusDays(1));
        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertEquals(0, slots.get(0).getStatus());
        assertEquals(schedule.getScheduleId(), slots.get(0).getScheduleId());
    }

    @Test
    @Order(5)
    @DisplayName("查询时段 — 无排班日期应返回空列表")
    void testGetAvailableSlotsNoSchedule() {
        List<TimeSlot> slots = scheduleService.getAvailableSlots("DOC_001", LocalDate.now().plusDays(30));
        assertNotNull(slots);
        assertTrue(slots.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("时段长度不能少于5分钟")
    void testSlotDurationMin5() {
        ScheduleCreateRequest request = buildRequest(DOCTOR_ID, LocalDate.now().plusDays(5), 0);
        request.setSlotDuration(3);

        assertThrows(Exception.class, () -> scheduleService.create(request));
    }

    private ScheduleCreateRequest buildRequest(String doctorId, LocalDate date, int shift) {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setDoctorId(doctorId);
        request.setDepartmentId(DEPARTMENT_ID);
        request.setScheduleDate(date);
        request.setWorkShift(shift);
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(12, 0));
        request.setSlotDuration(30);
        request.setMaxPatients(20);
        return request;
    }
}

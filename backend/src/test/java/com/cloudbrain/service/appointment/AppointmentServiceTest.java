package com.cloudbrain.service.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.AppointmentBookRequest;
import com.cloudbrain.dto.request.AppointmentCancelRequest;
import com.cloudbrain.dto.request.ScheduleCreateRequest;
import com.cloudbrain.entity.Appointment;
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
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private ScheduleService scheduleService;

    private static final String PATIENT_ID = "PAT_001";
    private static final String DOCTOR_ID = "DOC_001";
    private static final String DEPARTMENT_ID = "DEPT_001";
    private static LocalDate testDate;

    private static String bookedAppointmentId;
    private static String bookedSlotId;

    @BeforeAll
    static void setUp() {
        testDate = LocalDate.now().plusDays(1);
    }

    // ======================== 正案例 ========================

    @Test
    @Order(1)
    @DisplayName("完整预约流程 — 创建排班 → 预约 → 取消 → 时段释放")
    void testFullBookingFlow() {
        // 1. 创建排班（自动生成时段）
        ScheduleCreateRequest scheduleReq = new ScheduleCreateRequest();
        scheduleReq.setDoctorId(DOCTOR_ID);
        scheduleReq.setDepartmentId(DEPARTMENT_ID);
        scheduleReq.setScheduleDate(testDate);
        scheduleReq.setWorkShift(0);
        scheduleReq.setStartTime(LocalTime.of(8, 0));
        scheduleReq.setEndTime(LocalTime.of(12, 0));
        scheduleReq.setSlotDuration(30);
        scheduleReq.setMaxPatients(20);
        scheduleService.create(scheduleReq);

        // 2. 获取可用时段
        List<TimeSlot> slots = scheduleService.getAvailableSlots(DOCTOR_ID, testDate);
        assertFalse(slots.isEmpty());
        bookedSlotId = slots.get(0).getSlotId();

        // 3. 预约挂号
        AppointmentBookRequest bookReq = new AppointmentBookRequest();
        bookReq.setPatientId(PATIENT_ID);
        bookReq.setDoctorId(DOCTOR_ID);
        bookReq.setDepartmentId(DEPARTMENT_ID);
        bookReq.setSlotId(bookedSlotId);
        bookReq.setAppointmentType(0);
        bookReq.setSource(0);
        bookReq.setSymptoms("发热、咳嗽三天");

        Appointment appointment = appointmentService.book(bookReq);
        assertNotNull(appointment);
        assertNotNull(appointment.getAppointmentId());
        assertEquals(0, appointment.getStatus());
        assertEquals(0, appointment.getPaymentStatus());
        assertEquals(PATIENT_ID, appointment.getPatientId());
        bookedAppointmentId = appointment.getAppointmentId();

        // 4. 验证时段已被锁定
        slots = scheduleService.getAvailableSlots(DOCTOR_ID, testDate);
        boolean slotStillAvailable = slots.stream().anyMatch(s -> s.getSlotId().equals(bookedSlotId));
        assertFalse(slotStillAvailable, "预约后时段应不再可用");

        // 5. 取消预约
        AppointmentCancelRequest cancelReq = new AppointmentCancelRequest();
        cancelReq.setAppointmentId(bookedAppointmentId);
        cancelReq.setReason("病情好转，无需就诊");
        appointmentService.cancel(cancelReq);
    }

    @Test
    @Order(2)
    @DisplayName("预约详情 — 正常查询预约信息")
    void testGetDetail() {
        Appointment appointment = prepareAppointment();

        Appointment detail = appointmentService.getDetail(appointment.getAppointmentId());
        assertNotNull(detail);
        assertEquals(appointment.getAppointmentId(), detail.getAppointmentId());
        assertEquals(PATIENT_ID, detail.getPatientId());
    }

    @Test
    @Order(3)
    @DisplayName("预约列表 — 查到患者的历史预约")
    void testListByPatient() {
        prepareAppointment();

        List<Appointment> list = appointmentService.listByPatient(PATIENT_ID);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    // ======================== 负案例 ========================

    @Test
    @Order(4)
    @DisplayName("预约挂号 — 无效时段ID应抛异常")
    void testBookInvalidSlot() {
        AppointmentBookRequest request = new AppointmentBookRequest();
        request.setPatientId(PATIENT_ID);
        request.setDoctorId(DOCTOR_ID);
        request.setDepartmentId(DEPARTMENT_ID);
        request.setSlotId("SLOT_NOT_EXIST");
        request.setAppointmentType(0);
        request.setSource(0);

        assertThrows(BusinessException.class,
                () -> appointmentService.book(request));
    }

    @Test
    @Order(5)
    @DisplayName("查询详情 — 不存在的预约ID应抛异常")
    void testGetDetailNotFound() {
        assertThrows(BusinessException.class,
                () -> appointmentService.getDetail("NOT_EXIST"));
    }

    @Test
    @Order(6)
    @DisplayName("取消预约 — 不存在的预约ID应抛异常")
    void testCancelNotFound() {
        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setAppointmentId("NOT_EXIST");
        request.setReason("测试");

        assertThrows(BusinessException.class,
                () -> appointmentService.cancel(request));
    }

    // ======================== 辅助 ========================

    private Appointment prepareAppointment() {
        ScheduleCreateRequest scheduleReq = new ScheduleCreateRequest();
        scheduleReq.setDoctorId(DOCTOR_ID);
        scheduleReq.setDepartmentId(DEPARTMENT_ID);
        scheduleReq.setScheduleDate(testDate.plusDays(2));
        scheduleReq.setWorkShift(1);
        scheduleReq.setStartTime(LocalTime.of(14, 0));
        scheduleReq.setEndTime(LocalTime.of(17, 0));
        scheduleReq.setSlotDuration(30);
        scheduleReq.setMaxPatients(10);
        scheduleService.create(scheduleReq);

        List<TimeSlot> slots = scheduleService.getAvailableSlots(DOCTOR_ID, testDate.plusDays(2));
        assertFalse(slots.isEmpty());

        AppointmentBookRequest bookReq = new AppointmentBookRequest();
        bookReq.setPatientId(PATIENT_ID);
        bookReq.setDoctorId(DOCTOR_ID);
        bookReq.setDepartmentId(DEPARTMENT_ID);
        bookReq.setSlotId(slots.get(0).getSlotId());
        bookReq.setAppointmentType(0);
        bookReq.setSource(0);
        return appointmentService.book(bookReq);
    }
}

package com.cloudbrain.service.appointment;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.AppointmentBookRequest;
import com.cloudbrain.dto.request.PaymentCreateRequest;
import com.cloudbrain.dto.request.ScheduleCreateRequest;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Payment;
import com.cloudbrain.entity.TimeSlot;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class PaymentServiceTest {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private PaymentService paymentService;

    private static final String PATIENT_ID = "PAT_001";
    private static final String DOCTOR_ID = "DOC_001";
    private static final String DEPARTMENT_ID = "DEPT_001";
    private static LocalDate testDate;

    private String appointmentId;
    private String slotId;

    @BeforeAll
    static void setUp() {
        testDate = LocalDate.now().plusDays(1);
    }

    /**
     * 辅助方法：创建排班 → 获取可用时段 → 预约挂号
     */
    private Appointment prepareAppointment() {
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

        List<TimeSlot> slots = scheduleService.getAvailableSlots(DOCTOR_ID, testDate);
        assertFalse(slots.isEmpty(), "应有可用时段");
        slotId = slots.get(0).getSlotId();

        AppointmentBookRequest bookReq = new AppointmentBookRequest();
        bookReq.setPatientId(PATIENT_ID);
        bookReq.setDoctorId(DOCTOR_ID);
        bookReq.setDepartmentId(DEPARTMENT_ID);
        bookReq.setSlotId(slotId);
        bookReq.setAppointmentType(0);
        bookReq.setSource(0);
        bookReq.setSymptoms("头痛");

        Appointment appointment = appointmentService.book(bookReq);
        appointmentId = appointment.getAppointmentId();
        return appointment;
    }

    @Test
    @Order(1)
    @DisplayName("创建支付 — 正常支付流程")
    void testCreatePayment() {
        Appointment appointment = prepareAppointment();
        assertEquals(0, appointment.getPaymentStatus());

        PaymentCreateRequest payReq = new PaymentCreateRequest();
        payReq.setAppointmentId(appointmentId);
        payReq.setPatientId(PATIENT_ID);
        payReq.setPaymentMethod(2);

        Payment payment = paymentService.create(payReq);

        assertNotNull(payment);
        assertNotNull(payment.getPaymentId());
        assertEquals(1, payment.getPaymentStatus());
        assertNotNull(payment.getTradeNo());

        Payment queried = paymentService.getStatus(payment.getPaymentId());
        assertEquals(1, queried.getPaymentStatus());
    }

    @Test
    @Order(2)
    @DisplayName("重复支付 — 已支付的预约再次支付应抛异常")
    void testPayAlreadyPaid() {
        Appointment appointment = prepareAppointment();

        PaymentCreateRequest payReq = new PaymentCreateRequest();
        payReq.setAppointmentId(appointment.getAppointmentId());
        payReq.setPatientId(PATIENT_ID);
        payReq.setPaymentMethod(2);

        paymentService.create(payReq);

        assertThrows(BusinessException.class, () -> paymentService.create(payReq));
    }

    @Test
    @Order(3)
    @DisplayName("查询支付 — 不存在的支付ID应抛异常")
    void testGetStatusNotFound() {
        assertThrows(BusinessException.class,
                () -> paymentService.getStatus("PAY_NOT_EXIST"));
    }

    @Test
    @Order(4)
    @DisplayName("创建支付 — 不存在的预约应抛异常")
    void testPayNonExistentAppointment() {
        PaymentCreateRequest payReq = new PaymentCreateRequest();
        payReq.setAppointmentId("APT_NOT_EXIST");
        payReq.setPatientId(PATIENT_ID);
        payReq.setPaymentMethod(2);

        assertThrows(BusinessException.class,
                () -> paymentService.create(payReq));
    }
}

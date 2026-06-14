package com.cloudbrain.service.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.AppointmentBookRequest;
import com.cloudbrain.dto.request.AppointmentCancelRequest;
import com.cloudbrain.entity.Appointment;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ScheduleService scheduleService;

    private static String testAppointmentId;

    @Test
    @Order(1)
    @DisplayName("预约挂号 - 无效时段应抛异常")
    void testBookInvalidSlot() {
        AppointmentBookRequest request = new AppointmentBookRequest();
        request.setPatientId("PAT_001");
        request.setDoctorId("DOC_001");
        request.setDepartmentId("DEPT_001");
        request.setSlotId("SLOT_NOT_EXIST");
        request.setAppointmentType(0);
        request.setSource(0);

        assertThrows(BusinessException.class,
                () -> appointmentService.book(request));
    }

    @Test
    @Order(2)
    @DisplayName("预约列表 - 查询患者预约")
    void testListByPatient() {
        List<Appointment> list = appointmentService.listByPatient("PAT_001");
        assertNotNull(list);
    }

    @Test
    @Order(3)
    @DisplayName("预约列表 - 查询医生预约")
    void testListByDoctor() {
        List<Appointment> list = appointmentService.listByDoctor("DOC_001");
        assertNotNull(list);
    }

    @Test
    @Order(4)
    @DisplayName("查询不存在的预约应抛异常")
    void testGetDetailNotFound() {
        assertThrows(BusinessException.class,
                () -> appointmentService.getDetail("NOT_EXIST"));
    }

    @Test
    @Order(5)
    @DisplayName("取消不存在的预约应抛异常")
    void testCancelNotFound() {
        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setAppointmentId("NOT_EXIST");
        request.setReason("测试");

        assertThrows(BusinessException.class,
                () -> appointmentService.cancel(request));
    }
}

package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.AppointmentVO;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.service.appointment.AppointmentService;
import com.cloudbrain.service.appointment.DepartmentService;
import com.cloudbrain.service.appointment.DoctorService;
import com.cloudbrain.service.appointment.PaymentService;
import com.cloudbrain.service.appointment.ScheduleService;
import com.cloudbrain.service.patient.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AppointmentControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AppointmentController 单元测试")
class AppointmentControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.appointment", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ScheduleService scheduleService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("预约挂号")
        void testBook() throws Exception {
            Appointment apt = new Appointment();
            apt.setAppointmentId("APT_new001");
            apt.setTotalFee(new BigDecimal("25.00"));
            when(appointmentService.book(any())).thenReturn(apt);

            mockMvc.perform(post("/api/appointment/book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"patientId\":\"PAT_001\",\"doctorId\":\"DOC_001\"," +
                                    "\"departmentId\":\"DEPT_001\",\"slotId\":\"TS_001\"," +
                                    "\"appointmentType\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.appointmentId").value("APT_new001"));
        }

        @Test
        @DisplayName("取消预约")
        void testCancel() throws Exception {
            doNothing().when(appointmentService).cancel(any());

            mockMvc.perform(post("/api/appointment/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"appointmentId\":\"APT_test001\",\"reason\":\"个人原因\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("预约详情")
        void testDetail() throws Exception {
            Appointment apt = new Appointment();
            apt.setAppointmentId("APT_test001");
            apt.setPatientId("PAT_001");
            when(appointmentService.getDetail("APT_test001")).thenReturn(apt);

            mockMvc.perform(get("/api/appointment/detail").param("appointmentId", "APT_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.appointmentId").value("APT_test001"));
        }

        @Test
        @DisplayName("按患者查预约列表")
        void testListByPatient() throws Exception {
            AppointmentVO vo = AppointmentVO.builder().appointmentId("APT_test001").build();
            when(appointmentService.listByPatient("PAT_001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/appointment/list").param("patientId", "PAT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("按医生查预约列表")
        void testListByDoctor() throws Exception {
            AppointmentVO vo = AppointmentVO.builder().appointmentId("APT_test001").build();
            when(appointmentService.listByDoctor("DOC_001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/appointment/list").param("doctorId", "DOC_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("管理员预约列表")
        void testAdminList() throws Exception {
            when(appointmentService.listAll(isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/appointment/admin/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除预约")
        void testDelete() throws Exception {
            doNothing().when(appointmentService).delete("APT_test001");

            mockMvc.perform(delete("/api/appointment/delete").param("appointmentId", "APT_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("预约时段冲突")
        void testBookConflict() throws Exception {
            when(appointmentService.book(any()))
                    .thenThrow(new BusinessException("该时段已被预约"));

            mockMvc.perform(post("/api/appointment/book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"patientId\":\"PAT_001\",\"doctorId\":\"DOC_001\"," +
                                    "\"departmentId\":\"DEPT_001\",\"slotId\":\"TS_001\"," +
                                    "\"appointmentType\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("该时段已被预约"));
        }

        @Test
        @DisplayName("缺少列表查询参数")
        void testListMissingParam() throws Exception {
            mockMvc.perform(get("/api/appointment/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }
}

package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.DoctorVO;
import com.cloudbrain.dto.response.PatientInfoVO;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DoctorControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DoctorController 单元测试")
class DoctorControllerTest {

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
    private DoctorService doctorService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private AppointmentService appointmentService;

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
        @DisplayName("医生列表")
        void testList() throws Exception {
            DoctorVO vo = DoctorVO.builder().doctorId("DOC_001").realName("张医生").build();
            when(doctorService.listAll()).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/doctor/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("按科室查医生")
        void testListByDepartment() throws Exception {
            DoctorVO vo = DoctorVO.builder().doctorId("DOC_001").build();
            when(doctorService.listByDepartment("DEPT_001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/doctor/list").param("departmentId", "DEPT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("医生详情")
        void testDetail() throws Exception {
            DoctorVO vo = DoctorVO.builder().doctorId("DOC_001").realName("张医生").title("主任医师").build();
            when(doctorService.getDetail("DOC_001")).thenReturn(vo);

            mockMvc.perform(get("/api/doctor/detail").param("doctorId", "DOC_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.doctorId").value("DOC_001"));
        }

        @Test
        @DisplayName("当前登录医生信息")
        void testMe() throws Exception {
            DoctorVO vo = DoctorVO.builder().doctorId("DOC_001").realName("张医生").build();
            when(doctorService.getCurrentDoctor()).thenReturn(vo);

            mockMvc.perform(get("/api/doctor/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.doctorId").value("DOC_001"));
        }

        @Test
        @DisplayName("更新医生信息")
        void testUpdate() throws Exception {
            doNothing().when(doctorService).updateDoctor(eq("DOC_001"), any());

            mockMvc.perform(put("/api/doctor/update")
                            .param("doctorId", "DOC_001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"副主任医师\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("切换接诊状态")
        void testToggle() throws Exception {
            doNothing().when(doctorService).toggleAvailable("DOC_001");

            mockMvc.perform(put("/api/doctor/toggle").param("doctorId", "DOC_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("医生患者列表")
        void testPatients() throws Exception {
            DoctorVO doc = DoctorVO.builder().doctorId("DOC_001").build();
            when(doctorService.getCurrentDoctor()).thenReturn(doc);
            PatientInfoVO patient = PatientInfoVO.builder().patientId("PAT_001").name("张三").build();
            when(patientService.listPatientsByDoctor(eq("DOC_001"), isNull(), isNull(), isNull()))
                    .thenReturn(List.of(patient));

            mockMvc.perform(get("/api/doctor/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("医生不存在")
        void testDetailNotFound() throws Exception {
            when(doctorService.getDetail("DOC_NOT_EXIST"))
                    .thenThrow(new BusinessException("医生不存在"));

            mockMvc.perform(get("/api/doctor/detail").param("doctorId", "DOC_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("医生不存在"));
        }
    }
}

package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.entity.Department;
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

@SpringBootTest(classes = DepartmentControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DepartmentController 单元测试")
class DepartmentControllerTest {

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
    private DepartmentService departmentService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ScheduleService scheduleService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("科室列表")
        void testList() throws Exception {
            Department dept = new Department();
            dept.setDepartmentId("DEPT_001");
            dept.setName("内科");
            when(departmentService.listAll()).thenReturn(List.of(dept));

            mockMvc.perform(get("/api/department/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("按分类查科室")
        void testListByCategory() throws Exception {
            Department dept = new Department();
            dept.setDepartmentId("DEPT_001");
            dept.setName("内科");
            when(departmentService.listByCategory("临床")).thenReturn(List.of(dept));

            mockMvc.perform(get("/api/department/list").param("category", "临床"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("新增科室")
        void testCreate() throws Exception {
            Department dept = new Department();
            dept.setDepartmentId("DEPT_new001");
            dept.setName("新科室");
            when(departmentService.create(any())).thenReturn(dept);

            mockMvc.perform(post("/api/department/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"新科室\",\"category\":\"临床\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.departmentId").value("DEPT_new001"));
        }

        @Test
        @DisplayName("更新科室")
        void testUpdate() throws Exception {
            doNothing().when(departmentService).update(eq("DEPT_001"), any());

            mockMvc.perform(put("/api/department/update")
                            .param("departmentId", "DEPT_001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"内科更新\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除科室")
        void testDelete() throws Exception {
            doNothing().when(departmentService).delete("DEPT_001");

            mockMvc.perform(delete("/api/department/delete").param("departmentId", "DEPT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("科室不存在")
        void testDeleteNotFound() throws Exception {
            doThrow(new BusinessException("科室不存在"))
                    .when(departmentService).delete("DEPT_NOT_EXIST");

            mockMvc.perform(delete("/api/department/delete").param("departmentId", "DEPT_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("科室不存在"));
        }
    }
}

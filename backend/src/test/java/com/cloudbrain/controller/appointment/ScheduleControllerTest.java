package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.entity.Schedule;
import com.cloudbrain.entity.TimeSlot;
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

@SpringBootTest(classes = ScheduleControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ScheduleController 单元测试")
class ScheduleControllerTest {

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
    private ScheduleService scheduleService;

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

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("设置排班")
        void testSet() throws Exception {
            Schedule schedule = new Schedule();
            schedule.setScheduleId("SCH_new001");
            schedule.setDoctorId("DOC_001");
            when(scheduleService.create(any())).thenReturn(schedule);

            mockMvc.perform(post("/api/schedule/set")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"doctorId\":\"DOC_001\",\"departmentId\":\"DEPT_001\"," +
                                    "\"scheduleDate\":\"2026-07-01\",\"workShift\":0," +
                                    "\"startTime\":\"08:00\",\"endTime\":\"17:00\"," +
                                    "\"maxPatients\":20}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.scheduleId").value("SCH_new001"));
        }

        @Test
        @DisplayName("查询排班")
        void testQuery() throws Exception {
            Schedule schedule = new Schedule();
            schedule.setScheduleId("SCH_test001");
            when(scheduleService.query(eq("DOC_001"), isNull(), isNull())).thenReturn(List.of(schedule));

            mockMvc.perform(get("/api/schedule/query").param("doctorId", "DOC_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("可用时段")
        void testAvailable() throws Exception {
            TimeSlot slot = new TimeSlot();
            slot.setSlotId("TS_001");
            when(scheduleService.getAvailableSlots(eq("DOC_001"), any())).thenReturn(List.of(slot));

            mockMvc.perform(get("/api/schedule/available")
                            .param("doctorId", "DOC_001")
                            .param("date", "2026-07-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("排班冲突")
        void testSetConflict() throws Exception {
            when(scheduleService.create(any()))
                    .thenThrow(new BusinessException("排班时间冲突"));

            mockMvc.perform(post("/api/schedule/set")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"doctorId\":\"DOC_001\",\"departmentId\":\"DEPT_001\"," +
                                    "\"scheduleDate\":\"2026-07-01\",\"workShift\":0," +
                                    "\"startTime\":\"08:00\",\"endTime\":\"17:00\"," +
                                    "\"maxPatients\":20}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("排班时间冲突"));
        }
    }
}

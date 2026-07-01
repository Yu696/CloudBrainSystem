package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.entity.Payment;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PaymentController 单元测试")
class PaymentControllerTest {

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
    private PaymentService paymentService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private ScheduleService scheduleService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("创建支付")
        void testCreate() throws Exception {
            Payment payment = new Payment();
            payment.setPaymentId("PAY_new001");
            payment.setAmount(new BigDecimal("25.00"));
            when(paymentService.create(any())).thenReturn(payment);

            mockMvc.perform(post("/api/payment/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"appointmentId\":\"APT_test001\",\"patientId\":\"PAT_001\",\"paymentMethod\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.paymentId").value("PAY_new001"));
        }

        @Test
        @DisplayName("查询支付状态")
        void testStatus() throws Exception {
            Payment payment = new Payment();
            payment.setPaymentId("PAY_test001");
            payment.setPaymentStatus(1);
            when(paymentService.getStatus("PAY_test001")).thenReturn(payment);

            mockMvc.perform(get("/api/payment/status").param("paymentId", "PAY_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.paymentId").value("PAY_test001"));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("支付单不存在")
        void testStatusNotFound() throws Exception {
            when(paymentService.getStatus("PAY_NOT_EXIST"))
                    .thenThrow(new BusinessException("支付单不存在"));

            mockMvc.perform(get("/api/payment/status").param("paymentId", "PAY_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("支付单不存在"));
        }
    }
}

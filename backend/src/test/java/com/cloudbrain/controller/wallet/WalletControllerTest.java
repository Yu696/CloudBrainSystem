package com.cloudbrain.controller.wallet;

import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.ExaminationOrder;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.WalletTransaction;
import com.cloudbrain.mapper.*;
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

@SpringBootTest(classes = WalletControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WalletController  单元测试")
class WalletControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.wallet", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientMapper patientMapper;

    @MockBean
    private WalletTransactionMapper walletTransactionMapper;

    @MockBean
    private AppointmentMapper appointmentMapper;

    @MockBean
    private ExaminationOrderMapper examinationOrderMapper;

    @MockBean
    private DoctorMapper doctorMapper;

    @MockBean
    private UserMapper userMapper;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("获取钱包余额")
        void testBalance() throws Exception {
            Patient patient = new Patient();
            patient.setPatientId("PAT_001");
            patient.setBalance(new BigDecimal("100.00"));
            when(patientMapper.selectOne(any())).thenReturn(patient);

            mockMvc.perform(get("/api/wallet/balance").param("patientId", "PAT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.balance").value(100.00));
        }

        @Test
        @DisplayName("交易记录列表")
        void testTransactions() throws Exception {
            WalletTransaction tx = new WalletTransaction();
            tx.setTransactionId("TX_001");
            tx.setType(0);
            tx.setAmount(new BigDecimal("50.00"));
            when(walletTransactionMapper.selectList(any())).thenReturn(List.of(tx));

            mockMvc.perform(get("/api/wallet/transactions").param("patientId", "PAT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("待支付订单列表")
        void testPendingOrders() throws Exception {
            when(appointmentMapper.selectList(any())).thenReturn(List.of());
            when(examinationOrderMapper.selectList(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/wallet/pending-orders").param("patientId", "PAT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("异常操作")
    class ExceptionCases {
        @Test
        @DisplayName("患者不存在")
        void testBalancePatientNotFound() throws Exception {
            when(patientMapper.selectOne(any())).thenReturn(null);

            mockMvc.perform(get("/api/wallet/balance").param("patientId", "PAT_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("患者不存在"));
        }

        @Test
        @DisplayName("充值金额必须大于0")
        void testRechargeInvalidAmount() throws Exception {
            mockMvc.perform(post("/api/wallet/recharge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"patientId\":\"PAT_001\",\"amount\":-10}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("充值金额必须大于0"));
        }
    }
}

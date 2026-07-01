package com.cloudbrain.controller.medical;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.ExaminationOrderVO;
import com.cloudbrain.dto.response.ExaminationResultVO;
import com.cloudbrain.service.medical.ExaminationService;
import com.cloudbrain.service.medical.MedicalRecordService;
import com.cloudbrain.service.medical.PrescriptionService;
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

@SpringBootTest(classes = ExaminationControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ExaminationController 单元测试")
class ExaminationControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.medical", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExaminationService examinationService;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @MockBean
    private PrescriptionService prescriptionService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("开检查单")
        void testCreate() throws Exception {
            ExaminationOrderVO vo = ExaminationOrderVO.builder()
                    .orderId("EXO_new001").examName("血常规").examCategory(0).amount(new BigDecimal("35.00")).status(0).build();
            when(examinationService.createOrder(any())).thenReturn(vo);

            mockMvc.perform(post("/api/examination/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"recordId\":\"REC_test001\",\"examName\":\"血常规\",\"examCategory\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.orderId").value("EXO_new001"));
        }

        @Test
        @DisplayName("检查单详情")
        void testDetail() throws Exception {
            ExaminationOrderVO vo = ExaminationOrderVO.builder().orderId("EXO_test001").examName("血常规").build();
            when(examinationService.getDetail("EXO_test001")).thenReturn(vo);

            mockMvc.perform(get("/api/examination/detail").param("orderId", "EXO_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderId").value("EXO_test001"));
        }

        @Test
        @DisplayName("检查单列表")
        void testList() throws Exception {
            ExaminationOrderVO vo = ExaminationOrderVO.builder().orderId("EXO_test001").build();
            when(examinationService.listOrders("REC_test001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/examination/list").param("recordId", "REC_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("全部检查单列表")
        void testListAllOrders() throws Exception {
            when(examinationService.listAllOrders(isNull())).thenReturn(List.of());

            mockMvc.perform(get("/api/examination/all-orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("保存检查结果")
        void testSaveResult() throws Exception {
            ExaminationResultVO vo = ExaminationResultVO.builder().build();
            when(examinationService.saveResult(any())).thenReturn(vo);

            mockMvc.perform(post("/api/examination/result")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"orderId\":\"EXO_test001\",\"resultData\":\"正常\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("支付检查费")
        void testPay() throws Exception {
            doNothing().when(examinationService).payOrder("EXO_test001");

            mockMvc.perform(post("/api/examination/pay").param("orderId", "EXO_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除检查单")
        void testDelete() throws Exception {
            doNothing().when(examinationService).deleteOrder("EXO_test001");

            mockMvc.perform(delete("/api/examination/delete").param("orderId", "EXO_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("检查单不存在")
        void testDetailNotFound() throws Exception {
            when(examinationService.getDetail("EXO_NOT_EXIST"))
                    .thenThrow(new BusinessException("检查单不存在"));

            mockMvc.perform(get("/api/examination/detail").param("orderId", "EXO_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("检查单不存在"));
        }
    }
}

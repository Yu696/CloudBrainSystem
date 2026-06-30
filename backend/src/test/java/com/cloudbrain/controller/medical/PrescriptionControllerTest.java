package com.cloudbrain.controller.medical;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.PrescriptionItemVO;
import com.cloudbrain.dto.response.PrescriptionVO;
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

@SpringBootTest(classes = PrescriptionControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PrescriptionController 单元测试")
class PrescriptionControllerTest {

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
    private PrescriptionService prescriptionService;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @MockBean
    private ExaminationService examinationService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("开具处方")
        void testCreate() throws Exception {
            PrescriptionVO vo = PrescriptionVO.builder()
                    .prescriptionId("PRE_new001").totalAmount(new BigDecimal("50.00")).build();
            when(prescriptionService.createPrescription(any())).thenReturn(vo);

            mockMvc.perform(post("/api/prescription/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"recordId\":\"REC_test001\",\"patientId\":\"PAT_001\",\"prescriptionDesc\":\"感冒处方\"," +
                                    "\"items\":[{\"drugId\":\"DRUG_001\",\"drugName\":\"头孢\",\"quantity\":2,\"unitPrice\":25.00}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.prescriptionId").value("PRE_new001"));
        }

        @Test
        @DisplayName("处方列表")
        void testList() throws Exception {
            PrescriptionVO vo = PrescriptionVO.builder().prescriptionId("PRE_test001").build();
            when(prescriptionService.listPrescriptions("REC_test001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/prescription/list").param("recordId", "REC_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("处方详情")
        void testDetail() throws Exception {
            PrescriptionItemVO item = PrescriptionItemVO.builder()
                    .drugId("DRUG_001").drugName("头孢").build();
            PrescriptionVO vo = PrescriptionVO.builder()
                    .prescriptionId("PRE_test001").prescriptionDesc("感冒处方")
                    .items(List.of(item)).build();
            when(prescriptionService.getPrescriptionDetail("PRE_test001")).thenReturn(vo);

            mockMvc.perform(get("/api/prescription/detail").param("prescriptionId", "PRE_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.prescriptionId").value("PRE_test001"))
                    .andExpect(jsonPath("$.data.items[0].drugId").value("DRUG_001"));
        }

        @Test
        @DisplayName("更新处方")
        void testUpdate() throws Exception {
            PrescriptionVO vo = PrescriptionVO.builder().prescriptionId("PRE_test001").build();
            when(prescriptionService.updatePrescription(eq("PRE_test001"), any())).thenReturn(vo);

            mockMvc.perform(put("/api/prescription/update")
                            .param("prescriptionId", "PRE_test001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"recordId\":\"REC_test001\",\"prescriptionDesc\":\"更新处方\"," +
                                    "\"items\":[{\"drugId\":\"DRUG_001\",\"drugName\":\"头孢\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除处方")
        void testDelete() throws Exception {
            doNothing().when(prescriptionService).deletePrescription("PRE_test001");

            mockMvc.perform(delete("/api/prescription/delete").param("prescriptionId", "PRE_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("处方不存在")
        void testDetailNotFound() throws Exception {
            when(prescriptionService.getPrescriptionDetail("PRE_NOT_EXIST"))
                    .thenThrow(new BusinessException("处方不存在"));

            mockMvc.perform(get("/api/prescription/detail").param("prescriptionId", "PRE_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("处方不存在"));
        }
    }
}

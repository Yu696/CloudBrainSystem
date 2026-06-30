package com.cloudbrain.controller.medical;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.MedicalRecordVO;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MedicalRecordControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MedicalRecordController 单元测试")
class MedicalRecordControllerTest {

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
    private MedicalRecordService medicalRecordService;

    @MockBean
    private ExaminationService examinationService;

    @MockBean
    private PrescriptionService prescriptionService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("创建病历")
        void testCreate() throws Exception {
            MedicalRecordVO vo = MedicalRecordVO.builder().recordId("REC_new001").build();
            when(medicalRecordService.createRecord(any())).thenReturn(vo);

            mockMvc.perform(post("/api/medical-record/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"patientId\":\"PAT_001\",\"doctorId\":\"DOC_001\",\"chiefComplaint\":\"头痛\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.recordId").value("REC_new001"));
        }

        @Test
        @DisplayName("查询病历列表")
        void testList() throws Exception {
            MedicalRecordVO vo = MedicalRecordVO.builder().recordId("REC_test001").build();
            when(medicalRecordService.listRecords(eq("PAT_001"), isNull())).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/medical-record/list").param("patientId", "PAT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("查询病历详情")
        void testDetail() throws Exception {
            MedicalRecordVO vo = MedicalRecordVO.builder().recordId("REC_test001").chiefComplaint("头痛").build();
            when(medicalRecordService.getRecordDetail("REC_test001")).thenReturn(vo);

            mockMvc.perform(get("/api/medical-record/detail").param("recordId", "REC_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.recordId").value("REC_test001"));
        }

        @Test
        @DisplayName("更新病历")
        void testUpdate() throws Exception {
            doNothing().when(medicalRecordService).updateRecord(any());

            mockMvc.perform(put("/api/medical-record/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"recordId\":\"REC_test001\",\"chiefComplaint\":\"更新\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("完成病历")
        void testComplete() throws Exception {
            doNothing().when(medicalRecordService).completeRecord("REC_test001");

            mockMvc.perform(put("/api/medical-record/complete").param("recordId", "REC_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除病历")
        void testDelete() throws Exception {
            doNothing().when(medicalRecordService).deleteRecord("REC_test001");

            mockMvc.perform(delete("/api/medical-record/delete").param("recordId", "REC_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("病历不存在")
        void testDetailNotFound() throws Exception {
            when(medicalRecordService.getRecordDetail("REC_NOT_EXIST"))
                    .thenThrow(new BusinessException("病历不存在"));

            mockMvc.perform(get("/api/medical-record/detail").param("recordId", "REC_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("病历不存在"));
        }
    }
}

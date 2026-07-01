package com.cloudbrain.controller.patient;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.PatientCreateVO;
import com.cloudbrain.dto.response.PatientInfoVO;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PatientControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PatientController 单元测试")
class PatientControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.patient", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("创建患者档案")
        void testCreate() throws Exception {
            PatientCreateVO vo = new PatientCreateVO("PAT_new001", "MRN_test001");
            when(patientService.createPatient(any())).thenReturn(vo);

            mockMvc.perform(post("/api/patient/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"测试患者\",\"idCard\":\"110101199001011234\",\"phone\":\"13800001111\",\"gender\":0,\"birthDate\":\"1990-01-01\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.patientId").value("PAT_new001"));
        }

        @Test
        @DisplayName("查询患者信息")
        void testInfo() throws Exception {
            PatientInfoVO vo = PatientInfoVO.builder().patientId("PAT_001").name("张三").build();
            when(patientService.getPatientInfo("PAT_001")).thenReturn(vo);

            mockMvc.perform(get("/api/patient/info").param("patientId", "PAT_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("张三"));
        }

        @Test
        @DisplayName("患者列表")
        void testList() throws Exception {
            PatientInfoVO vo = PatientInfoVO.builder().patientId("PAT_001").build();
            when(patientService.listPatients(any(), any(), any())).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/patient/list").param("name", "张"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新患者档案")
        void testUpdate() throws Exception {
            doNothing().when(patientService).updatePatient(any());

            mockMvc.perform(put("/api/patient/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"patientId\":\"PAT_001\",\"name\":\"张三改\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("校验身份证")
        void testCheckIdCard() throws Exception {
            when(patientService.checkIdCard("110101199001011234")).thenReturn(true);

            mockMvc.perform(get("/api/patient/check-idcard").param("idCard", "110101199001011234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.exists").value(true));
        }

        @Test
        @DisplayName("根据用户ID查找患者")
        void testFindByUser() throws Exception {
            PatientInfoVO vo = PatientInfoVO.builder().patientId("PAT_001").build();
            when(patientService.findByUserId("USR_pat001")).thenReturn(vo);

            mockMvc.perform(get("/api/patient/find-by-user").param("userId", "USR_pat001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.patientId").value("PAT_001"));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("身份证重复")
        void testDuplicateIdCard() throws Exception {
            when(patientService.createPatient(any()))
                    .thenThrow(new BusinessException("身份证号已存在"));

            mockMvc.perform(post("/api/patient/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"重复\",\"idCard\":\"110101199001011234\",\"birthDate\":\"1990-01-01\",\"phone\":\"13800001111\",\"gender\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("身份证号已存在"));
        }
    }
}

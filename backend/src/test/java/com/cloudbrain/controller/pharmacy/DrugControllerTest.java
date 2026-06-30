package com.cloudbrain.controller.pharmacy;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.pharmacy.DispenseResultVO;
import com.cloudbrain.dto.response.pharmacy.DrugVO;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;
import com.cloudbrain.dto.response.pharmacy.WarehouseVO;
import com.cloudbrain.entity.ShipRecord;
import com.cloudbrain.entity.Warehouse;
import com.cloudbrain.mapper.*;
import com.cloudbrain.service.pharmacy.DispenseService;
import com.cloudbrain.service.pharmacy.DrugService;
import com.cloudbrain.service.pharmacy.InventoryService;
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

@SpringBootTest(classes = DrugControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DrugController 单元测试")
class DrugControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.pharmacy", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrugService drugService;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private DispenseService dispenseService;

    @MockBean
    private ShipRecordMapper shipRecordMapper;

    @MockBean
    private WarehouseMapper warehouseMapper;

    @MockBean
    private PrescriptionMapper prescriptionMapper;

    @MockBean
    private PrescriptionItemMapper prescriptionItemMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private PatientMapper patientMapper;

    @MockBean
    private DoctorMapper doctorMapper;

    @Nested
    @DisplayName("药品管理")
    class DrugManagement {
        @Test
        @DisplayName("新增药品")
        void testAdd() throws Exception {
            when(drugService.add(any())).thenReturn("DRUG_new001");

            mockMvc.perform(post("/api/drug/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"drugName\":\"新药\",\"drugCode\":\"NEW001\",\"spec\":\"10mg\",\"unit\":\"盒\",\"unitPrice\":15.00,\"prescriptionType\":1}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("DRUG_new001"));
        }

        @Test
        @DisplayName("修改药品")
        void testUpdate() throws Exception {
            doNothing().when(drugService).update(any());

            mockMvc.perform(put("/api/drug/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"drugId\":\"DRUG_001\",\"drugName\":\"更新药品\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除药品")
        void testDelete() throws Exception {
            doNothing().when(drugService).delete("DRUG_001");

            mockMvc.perform(delete("/api/drug/delete").param("drugId", "DRUG_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("全部药品列表")
        void testAllDrugs() throws Exception {
            DrugVO vo = DrugVO.builder().drugId("DRUG_001").drugName("头孢呋辛酯片").build();
            when(drugService.all()).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/drug/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("搜索药品")
        void testSearch() throws Exception {
            DrugVO vo = DrugVO.builder().drugId("DRUG_001").drugName("头孢").build();
            PageResult<DrugVO> page = new PageResult<>();
            page.setRecords(List.of(vo));
            page.setTotal(1L);
            when(drugService.search(eq("头孢"), isNull(), isNull(), eq(1), eq(20))).thenReturn(page);

            mockMvc.perform(get("/api/drug/search").param("keyword", "头孢"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("药品详情")
        void testDetail() throws Exception {
            DrugVO vo = DrugVO.builder().drugId("DRUG_001").drugName("头孢呋辛酯片").build();
            when(drugService.getDetail("DRUG_001")).thenReturn(vo);

            mockMvc.perform(get("/api/drug/detail").param("drugId", "DRUG_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.drugId").value("DRUG_001"));
        }
    }

    @Nested
    @DisplayName("库存管理")
    class InventoryManagement {
        @Test
        @DisplayName("查询库存")
        void testStock() throws Exception {
            StockVO vo = StockVO.builder().drugId("DRUG_001").currentStock(100).build();
            when(inventoryService.getStock("DRUG_001")).thenReturn(vo);

            mockMvc.perform(get("/api/drug/stock").param("drugId", "DRUG_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("库存预警")
        void testLowStock() throws Exception {
            StockAlertVO alert = StockAlertVO.builder().drugName("头孢").alertType(0).build();
            when(inventoryService.listAlerts(isNull())).thenReturn(List.of(alert));

            mockMvc.perform(get("/api/drug/low-stock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("调整库存")
        void testAdjustStock() throws Exception {
            doNothing().when(inventoryService).adjustStock(anyString(), anyInt(), any(), any(), any(), any(), anyInt(), anyInt());

            mockMvc.perform(put("/api/drug/stock/adjust")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"drugId\":\"DRUG_001\",\"quantity\":10,\"warehouseId\":\"WH_001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("发药出库")
    class Dispense {
        @Test
        @DisplayName("发药出库")
        void testDispense() throws Exception {
            DispenseResultVO vo = DispenseResultVO.builder()
                    .recordId("SHP_new001").prescriptionId("PRE_test001").build();
            when(dispenseService.dispense(any())).thenReturn(vo);

            mockMvc.perform(post("/api/drug/dispense")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"prescriptionId\":\"PRE_test001\",\"patientId\":\"PAT_001\"," +
                                    "\"items\":[{\"drugId\":\"DRUG_001\",\"quantity\":1}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("仓库管理")
    class WarehouseManagement {
        @Test
        @DisplayName("仓库列表")
        void testWarehouseList() throws Exception {
            Warehouse wh = new Warehouse();
            wh.setWarehouseId("WH_001");
            wh.setName("主药库");
            wh.setType(0);
            wh.setStatus(1);
            when(warehouseMapper.selectList(any())).thenReturn(List.of(wh));

            mockMvc.perform(get("/api/drug/warehouse"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("药品不存在")
        void testDrugNotFound() throws Exception {
            when(drugService.getDetail("DRUG_NOT_EXIST"))
                    .thenThrow(new BusinessException("药品不存在"));

            mockMvc.perform(get("/api/drug/detail").param("drugId", "DRUG_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("药品不存在"));
        }
    }
}

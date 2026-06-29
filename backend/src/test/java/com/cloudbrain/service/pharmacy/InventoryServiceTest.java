package com.cloudbrain.service.pharmacy;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("查询药品库存")
    void testGetStock() {
        StockVO stock = inventoryService.getStock("DRUG_001");
        assertNotNull(stock);
        assertEquals("DRUG_001", stock.getDrugId());
        assertEquals("阿莫西林胶囊", stock.getDrugName());
        assertTrue(stock.getCurrentStock() > 0);
        assertEquals(10, stock.getMinStock());
        assertEquals(1000, stock.getMaxStock());
        assertNotNull(stock.getBatchNo());
    }

    @Test
    @DisplayName("查询不存在的药品库存应抛异常")
    void testGetStockNotFound() {
        assertThrows(BusinessException.class, () -> inventoryService.getStock("DRUG_NOT_EXIST"));
    }

    @Test
    @DisplayName("库存预警列表—低库存筛选")
    void testListAlertsByLowStock() {
        // 种子数据中没有预警记录，先验证接口正常返回空列表或已有记录
        List<StockAlertVO> alerts = inventoryService.listAlerts(0);
        assertNotNull(alerts);
    }

    @Test
    @DisplayName("库存预警列表—全类型")
    void testListAllAlerts() {
        List<StockAlertVO> alerts = inventoryService.listAlerts(null);
        assertNotNull(alerts);
    }

    @Test
    @DisplayName("标记不存在的预警应抛异常")
    void testHandleAlertNotFound() {
        assertThrows(BusinessException.class, () -> inventoryService.handleAlert(99999L));
    }

    @Test
    @DisplayName("按仓库查询库存列表")
    void testListByWarehouse() {
        var result = inventoryService.listByWarehouse("WH_001", null, 1, 10);
        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        assertEquals("WH_001", result.getRecords().get(0).getWarehouseId());
    }
}

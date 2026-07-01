package com.cloudbrain.service.pharmacy;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.pharmacy.DispenseRequest;
import com.cloudbrain.dto.response.pharmacy.DispenseResultVO;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;
import com.cloudbrain.entity.Prescription;
import com.cloudbrain.entity.WalletTransaction;
import com.cloudbrain.mapper.PrescriptionMapper;
import com.cloudbrain.mapper.WalletTransactionMapper;
import com.cloudbrain.util.UUIDUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class DispenseServiceTest {

    @Autowired
    private DispenseService dispenseService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private WalletTransactionMapper walletTxMapper;

    /** 为指定处方ID创建已审核处方 + 支付记录，使发药校验通过 */
    private void preparePrescriptionForDispense(String prescriptionId, String patientId) {
        Prescription p = new Prescription();
        p.setPrescriptionId(prescriptionId);
        p.setPatientId(patientId);
        p.setRecordId("REC_test001");
        p.setDoctorId("DOC_001");
        p.setStatus(2); // 已审核
        p.setAuditStatus(1);
        p.setTotalAmount(BigDecimal.valueOf(50));
        prescriptionMapper.insert(p);

        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionId(UUIDUtil.generateTransactionId());
        tx.setPatientId(patientId);
        tx.setType(2); // 药费
        tx.setAmount(new BigDecimal("-50"));
        tx.setBalanceAfter(new BigDecimal("50"));
        tx.setRefId(prescriptionId);
        tx.setRemark("药费：处方" + prescriptionId);
        walletTxMapper.insert(tx);
    }

    @Test
    @Order(1)
    @DisplayName("正常发药—库存扣减 + 发药记录生成")
    void testDispense() {
        preparePrescriptionForDispense("PRS_TEST_001", "PAT_001");
        StockVO before = inventoryService.getStock("DRUG_001");
        int beforeStock = before.getCurrentStock();

        DispenseRequest request = new DispenseRequest();
        request.setPrescriptionId("PRS_TEST_001");
        request.setPatientId("PAT_001");
        DispenseRequest.DispenseItem item = new DispenseRequest.DispenseItem();
        item.setDrugId("DRUG_001");
        item.setQuantity(2);
        request.setItems(List.of(item));

        DispenseResultVO result = dispenseService.dispense(request);
        assertNotNull(result.getRecordId());
        assertEquals(2, result.getShipNum());
        assertNotNull(result.getShipTime());
        assertEquals(0, result.getPrintStatus());

        StockVO after = inventoryService.getStock("DRUG_001");
        assertEquals(beforeStock - 2, after.getCurrentStock());
    }

    @Test
    @Order(2)
    @DisplayName("库存不足应抛异常")
    void testInsufficientStock() {
        preparePrescriptionForDispense("PRS_TEST_002", "PAT_001");
        DispenseRequest request = new DispenseRequest();
        request.setPrescriptionId("PRS_TEST_002");
        request.setPatientId("PAT_001");
        DispenseRequest.DispenseItem item = new DispenseRequest.DispenseItem();
        item.setDrugId("DRUG_001");
        item.setQuantity(99999); // 远超库存
        request.setItems(List.of(item));

        assertThrows(BusinessException.class, () -> dispenseService.dispense(request));
    }

    @Test
    @Order(3)
    @DisplayName("过期药品发药应抛异常")
    void testExpiredDrug() {
        preparePrescriptionForDispense("PRS_TEST_003", "PAT_001");
        // DRUG_003 的批号 20240120 已于 2026-01-20 过期
        DispenseRequest request = new DispenseRequest();
        request.setPrescriptionId("PRS_TEST_003");
        request.setPatientId("PAT_001");
        DispenseRequest.DispenseItem item = new DispenseRequest.DispenseItem();
        item.setDrugId("DRUG_003");
        item.setQuantity(1);
        request.setItems(List.of(item));

        assertThrows(BusinessException.class, () -> dispenseService.dispense(request));
    }

    @Test
    @Order(4)
    @DisplayName("发药后低于预警线自动生成预警")
    void testLowStockAlertAfterDispense() {
        preparePrescriptionForDispense("PRS_TEST_004", "PAT_001");
        // DRUG_003 当前库存=5，预警线=10。发1个后=4，低于预警线
        // 但 DRUG_003 已过期不能用，改用 DRUG_004 (库存200, 预警线20)
        // 需要大量发药才会触发预警，先查库存
        StockVO before = inventoryService.getStock("DRUG_004");

        // 发药到刚好等于预警线，但不会低于（等于是不会触发预警的，要<=minStock才触发）
        // 发181个：200-181=19 < 20 预警线，触发
        int qty = before.getCurrentStock() - before.getMinStock() + 1;

        DispenseRequest request = new DispenseRequest();
        request.setPrescriptionId("PRS_TEST_004");
        request.setPatientId("PAT_001");
        DispenseRequest.DispenseItem item = new DispenseRequest.DispenseItem();
        item.setDrugId("DRUG_004");
        item.setQuantity(qty);
        request.setItems(List.of(item));

        dispenseService.dispense(request);

        // 检查预警
        List<StockAlertVO> alerts = inventoryService.listAlerts(0); // type=0 低库存
        boolean hasAlert = alerts.stream().anyMatch(a -> "DRUG_004".equals(a.getDrugId()));
        assertTrue(hasAlert, "应自动生成 DRUG_004 的低库存预警");
    }

    @Test
    @Order(5)
    @DisplayName("不存在的药品应抛异常")
    void testDrugNotFound() {
        DispenseRequest request = new DispenseRequest();
        request.setPrescriptionId("PRS_TEST_005");
        request.setPatientId("PAT_001");
        DispenseRequest.DispenseItem item = new DispenseRequest.DispenseItem();
        item.setDrugId("DRUG_NOT_EXIST");
        item.setQuantity(1);
        request.setItems(List.of(item));

        assertThrows(BusinessException.class, () -> dispenseService.dispense(request));
    }
}

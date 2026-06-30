package com.cloudbrain.service.medical;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PrescriptionCreateRequest;
import com.cloudbrain.dto.response.PrescriptionVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 处方服务测试。
 * 所有写操作通过 @Transactional 自动回滚，不污染数据库。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class PrescriptionServiceTest {

    @Autowired
    private PrescriptionService prescriptionService;

    private static final String EXISTING_RECORD = "REC_test001";  // 有处方 PRS_test001
    private static final String RECORD_WITHOUT_PRESCRIPTIONS = "REC_test002";

    // ======================== 创建处方 ========================

    @Test
    @Order(1)
    @DisplayName("创建处方 — 包含明细的正常创建")
    void testCreatePrescriptionWithItems() {
        PrescriptionCreateRequest req = new PrescriptionCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setPrescriptionDesc("测试处方说明");

        PrescriptionCreateRequest.PrescriptionItemRequest item1 = new PrescriptionCreateRequest.PrescriptionItemRequest();
        item1.setDrugId("DRUG_001");
        item1.setDrugName("头孢呋辛酯片");
        item1.setSpec("0.25g×12片");
        item1.setDosage("0.25g");
        item1.setFrequency("每日2次");
        item1.setAdministration("口服");
        item1.setDays(7);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("35.00"));

        PrescriptionCreateRequest.PrescriptionItemRequest item2 = new PrescriptionCreateRequest.PrescriptionItemRequest();
        item2.setDrugId("DRUG_002");
        item2.setDrugName("布洛芬缓释胶囊");
        item2.setSpec("0.3g×20粒");
        item2.setDosage("0.3g");
        item2.setFrequency("每日1次");
        item2.setAdministration("口服");
        item2.setDays(3);
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("25.00"));

        req.setItems(List.of(item1, item2));

        PrescriptionVO vo = prescriptionService.createPrescription(req);
        assertNotNull(vo);
        assertNotNull(vo.getPrescriptionId());
        assertTrue(vo.getPrescriptionId().startsWith("PRS_"));
        assertEquals(new BigDecimal("95.00"), vo.getTotalAmount()); // 35*2 + 25*1
    }

    @Test
    @Order(2)
    @DisplayName("创建处方 — 不存在的病历抛异常")
    void testCreatePrescriptionInvalidRecord() {
        PrescriptionCreateRequest req = new PrescriptionCreateRequest();
        req.setRecordId("REC_NOT_EXIST");
        req.setItems(List.of());

        assertThrows(BusinessException.class, () -> prescriptionService.createPrescription(req));
    }

    @Test
    @Order(3)
    @DisplayName("创建处方 — 空明细项")
    void testCreatePrescriptionEmptyItems() {
        PrescriptionCreateRequest req = new PrescriptionCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setItems(List.of());

        PrescriptionVO vo = prescriptionService.createPrescription(req);
        assertNotNull(vo);
        assertEquals(BigDecimal.ZERO, vo.getTotalAmount());
    }

    // ======================== 查询处方 ========================

    @Test
    @Order(4)
    @DisplayName("处方列表 — 按病历查询")
    void testListPrescriptions() {
        List<PrescriptionVO> list = prescriptionService.listPrescriptions(EXISTING_RECORD);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "REC_test001 应有处方 PRS_test001");
    }

    @Test
    @Order(5)
    @DisplayName("处方列表 — 无处方病历返回空列表")
    void testListPrescriptionsEmpty() {
        List<PrescriptionVO> list = prescriptionService.listPrescriptions(RECORD_WITHOUT_PRESCRIPTIONS);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("处方详情 — 包含明细和药品信息")
    void testGetPrescriptionDetailExisting() {
        PrescriptionVO vo = prescriptionService.getPrescriptionDetail("PRS_test001");
        assertNotNull(vo);
        assertEquals("PRS_test001", vo.getPrescriptionId());
        assertNotNull(vo.getItems());
        assertFalse(vo.getItems().isEmpty(), "应包含处方明细");
    }

    @Test
    @Order(7)
    @DisplayName("处方详情 — 不存在的处方抛异常")
    void testGetPrescriptionDetailNotFound() {
        assertThrows(BusinessException.class,
                () -> prescriptionService.getPrescriptionDetail("PRS_NOT_EXIST"));
    }

    // ======================== 更新处方 ========================

    @Test
    @Order(8)
    @DisplayName("更新处方 — 修改描述和明细")
    void testUpdatePrescription() {
        // 先创建
        PrescriptionCreateRequest createReq = new PrescriptionCreateRequest();
        createReq.setRecordId(EXISTING_RECORD);
        createReq.setPrescriptionDesc("原始描述");
        PrescriptionCreateRequest.PrescriptionItemRequest item = new PrescriptionCreateRequest.PrescriptionItemRequest();
        item.setDrugId("DRUG_001");
        item.setDrugName("头孢呋辛酯片");
        item.setDosage("0.25g");
        item.setFrequency("每日2次");
        item.setDays(5);
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("35.00"));
        createReq.setItems(List.of(item));
        PrescriptionVO created = prescriptionService.createPrescription(createReq);

        // 更新
        PrescriptionCreateRequest updateReq = new PrescriptionCreateRequest();
        updateReq.setRecordId(EXISTING_RECORD);
        updateReq.setPrescriptionDesc("更新后的描述");
        PrescriptionCreateRequest.PrescriptionItemRequest newItem = new PrescriptionCreateRequest.PrescriptionItemRequest();
        newItem.setDrugId("DRUG_002");
        newItem.setDrugName("布洛芬缓释胶囊");
        newItem.setDosage("0.3g");
        newItem.setFrequency("每日1次");
        newItem.setDays(3);
        newItem.setQuantity(3);
        newItem.setUnitPrice(new BigDecimal("25.00"));
        updateReq.setItems(List.of(newItem));

        PrescriptionVO updated = prescriptionService.updatePrescription(created.getPrescriptionId(), updateReq);
        assertEquals("更新后的描述", updated.getPrescriptionDesc());
        assertEquals(new BigDecimal("75.00"), updated.getTotalAmount());

        // 验证明细也更新了
        PrescriptionVO detail = prescriptionService.getPrescriptionDetail(created.getPrescriptionId());
        assertEquals(1, detail.getItems().size());
    }

    @Test
    @Order(9)
    @DisplayName("更新处方 — 不存在的处方抛异常")
    void testUpdatePrescriptionNotFound() {
        PrescriptionCreateRequest req = new PrescriptionCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setItems(List.of());

        assertThrows(BusinessException.class,
                () -> prescriptionService.updatePrescription("PRS_NOT_EXIST", req));
    }

    // ======================== 删除处方 ========================

    @Test
    @Order(10)
    @DisplayName("删除处方 — 新建后删除")
    void testDeletePrescription() {
        PrescriptionCreateRequest req = new PrescriptionCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setItems(List.of());
        PrescriptionVO created = prescriptionService.createPrescription(req);

        prescriptionService.deletePrescription(created.getPrescriptionId());

        assertThrows(BusinessException.class,
                () -> prescriptionService.getPrescriptionDetail(created.getPrescriptionId()));
    }

    @Test
    @Order(11)
    @DisplayName("删除处方 — 不存在的处方抛异常")
    void testDeletePrescriptionNotFound() {
        assertThrows(BusinessException.class,
                () -> prescriptionService.deletePrescription("PRS_NOT_EXIST"));
    }
}

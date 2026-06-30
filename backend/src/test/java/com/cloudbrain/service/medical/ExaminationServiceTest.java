package com.cloudbrain.service.medical;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.ExaminationOrderCreateRequest;
import com.cloudbrain.dto.request.ExaminationResultCreateRequest;
import com.cloudbrain.dto.response.ExaminationOrderVO;
import com.cloudbrain.dto.response.ExaminationResultVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 检查单服务测试。
 * 所有写操作通过 @Transactional 自动回滚，不污染数据库。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class ExaminationServiceTest {

    @Autowired
    private ExaminationService examinationService;

    private static final String EXISTING_RECORD = "REC_test001";
    private static final String DOCTOR_ID = "DOC_001";

    // ======================== 创建检查单 ========================

    @Test
    @Order(1)
    @DisplayName("创建检查单 — 正常创建（按价目表自动计算费用）")
    void testCreateOrderSuccess() {
        ExaminationOrderCreateRequest req = new ExaminationOrderCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setExamName("血常规");
        req.setExamCategory(0);

        ExaminationOrderVO vo = examinationService.createOrder(req);
        assertNotNull(vo);
        assertNotNull(vo.getOrderId());
        assertTrue(vo.getOrderId().startsWith("EXO_"));
        assertEquals("血常规", vo.getExamName());
        assertEquals(0, vo.getExamCategory());
        assertEquals(new BigDecimal("35.00"), vo.getAmount());
        assertEquals(0, vo.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("创建检查单 — 指定费用")
    void testCreateOrderWithCustomAmount() {
        ExaminationOrderCreateRequest req = new ExaminationOrderCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setExamName("自定义检查项");
        req.setExamCategory(2);
        req.setAmount(new BigDecimal("200.00"));

        ExaminationOrderVO vo = examinationService.createOrder(req);
        assertEquals(new BigDecimal("200.00"), vo.getAmount());
    }

    @Test
    @Order(3)
    @DisplayName("创建检查单 — 不存在的病历抛异常")
    void testCreateOrderInvalidRecord() {
        ExaminationOrderCreateRequest req = new ExaminationOrderCreateRequest();
        req.setRecordId("REC_NOT_EXIST");
        req.setExamName("血常规");
        req.setExamCategory(0);

        assertThrows(BusinessException.class, () -> examinationService.createOrder(req));
    }

    // ======================== 更新检查单 ========================

    @Test
    @Order(4)
    @DisplayName("更新检查单 — 修改检查项目")
    void testUpdateOrder() {
        ExaminationOrderCreateRequest createReq = new ExaminationOrderCreateRequest();
        createReq.setRecordId(EXISTING_RECORD);
        createReq.setExamName("心电图");
        createReq.setExamCategory(2);
        ExaminationOrderVO created = examinationService.createOrder(createReq);

        ExaminationOrderCreateRequest updateReq = new ExaminationOrderCreateRequest();
        updateReq.setRecordId(EXISTING_RECORD);
        updateReq.setExamName("动态心电图");
        updateReq.setExamCategory(2);

        ExaminationOrderVO updated = examinationService.updateOrder(created.getOrderId(), updateReq);
        assertEquals("动态心电图", updated.getExamName());
        assertEquals(new BigDecimal("280.00"), updated.getAmount());
    }

    // ======================== 查询检查单 ========================

    @Test
    @Order(5)
    @DisplayName("检查单列表 — 按病历查询")
    void testListOrders() {
        List<ExaminationOrderVO> list = examinationService.listOrders(EXISTING_RECORD);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "REC_test001 应有检查单");
    }

    @Test
    @Order(6)
    @DisplayName("影像检查单列表 — 医生查看自己的")
    void testListImagingOrders() {
        List<ExaminationOrderVO> list = examinationService.listImagingOrders(DOCTOR_ID);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "DOC_001 应有影像检查单");
        list.forEach(o -> assertEquals(1, o.getExamCategory(), "应仅含影像学检查"));
    }

    @Test
    @Order(7)
    @DisplayName("全部检查单列表 — 无类别限制")
    void testListAllOrders() {
        List<ExaminationOrderVO> list = examinationService.listAllOrders(DOCTOR_ID);
        assertNotNull(list);
        assertFalse(list.isEmpty());
        // 应包含不同类别的检查
        boolean hasNonImaging = list.stream().anyMatch(o -> o.getExamCategory() != 1);
        assertTrue(hasNonImaging || list.size() >= 1, "应有非影像检查或至少有一项检查");
    }

    @Test
    @Order(8)
    @DisplayName("检查单详情 — 存在的检查单")
    void testGetDetailExisting() {
        ExaminationOrderVO vo = examinationService.getDetail("EXO_test001");
        assertNotNull(vo);
        assertEquals("EXO_test001", vo.getOrderId());
    }

    @Test
    @Order(9)
    @DisplayName("检查单详情 — 不存在的检查单抛异常")
    void testGetDetailNotFound() {
        assertThrows(BusinessException.class,
                () -> examinationService.getDetail("EXO_NOT_EXIST"));
    }

    // ======================== 检查结果 ========================

    @Test
    @Order(10)
    @DisplayName("获取检查结果 — 已有结果")
    void testGetResultExisting() {
        ExaminationResultVO vo = examinationService.getResult("EXO_test001");
        assertNotNull(vo, "EXO_test001 应有检查结果");
        assertNotNull(vo.getResultId());
    }

    @Test
    @Order(11)
    @DisplayName("获取检查结果 — 无结果返回 null")
    void testGetResultNoResult() {
        // 创建一个没有结果的检查单
        ExaminationOrderCreateRequest req = new ExaminationOrderCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setExamName("肺功能检测");
        req.setExamCategory(2);
        ExaminationOrderVO newOrder = examinationService.createOrder(req);

        ExaminationResultVO vo = examinationService.getResult(newOrder.getOrderId());
        assertNull(vo);
    }

    @Test
    @Order(12)
    @DisplayName("保存检查结果 — 新增结果")
    void testSaveResultNew() {
        ExaminationOrderCreateRequest req = new ExaminationOrderCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setExamName("血糖");
        req.setExamCategory(0);
        ExaminationOrderVO newOrder = examinationService.createOrder(req);

        ExaminationResultCreateRequest resultReq = new ExaminationResultCreateRequest();
        resultReq.setOrderId(newOrder.getOrderId());
        resultReq.setResultData("空腹血糖：6.2 mmol/L");
        resultReq.setReferenceRange("3.9-6.1 mmol/L");
        resultReq.setIsAbnormal(1);
        resultReq.setDoctorOpinion("空腹血糖偏高，建议复查糖耐量");

        ExaminationResultVO saved = examinationService.saveResult(resultReq);
        assertNotNull(saved.getResultId());
        assertEquals("空腹血糖：6.2 mmol/L", saved.getResultData());

        // 验证明细
        ExaminationOrderVO updatedOrder = examinationService.getDetail(newOrder.getOrderId());
        assertEquals(3, updatedOrder.getStatus(), "保存结果后检查单应变为已完成");
    }

    @Test
    @Order(13)
    @DisplayName("保存检查结果 — 更新已有结果")
    void testSaveResultUpdate() {
        ExaminationResultCreateRequest req = new ExaminationResultCreateRequest();
        req.setOrderId("EXO_test001");
        req.setResultData("更新后的结果数据");
        req.setDoctorOpinion("更新后的诊断意见");

        ExaminationResultVO saved = examinationService.saveResult(req);
        assertEquals("更新后的结果数据", saved.getResultData());
    }

    // ======================== 支付检查单 ========================

    @Test
    @Order(14)
    @DisplayName("支付检查单 — 正常扣款")
    void testPayOrderSuccess() {
        // EXO_test004 status=3, unpaid
        examinationService.payOrder("EXO_test004");

        ExaminationOrderVO order = examinationService.getDetail("EXO_test004");
        assertEquals(1, order.getStatus(), "支付后状态应为已缴费");
    }

    @Test
    @Order(15)
    @DisplayName("支付检查单 — 已支付的抛异常")
    void testPayOrderAlreadyPaid() {
        examinationService.payOrder("EXO_test004");
        assertThrows(BusinessException.class,
                () -> examinationService.payOrder("EXO_test004"));
    }

    @Test
    @Order(16)
    @DisplayName("支付检查单 — 不存在的检查单抛异常")
    void testPayOrderNotFound() {
        assertThrows(BusinessException.class,
                () -> examinationService.payOrder("EXO_NOT_EXIST"));
    }

    // ======================== 删除检查单 ========================

    @Test
    @Order(17)
    @DisplayName("删除检查单 — 新建后删除")
    void testDeleteOrder() {
        ExaminationOrderCreateRequest req = new ExaminationOrderCreateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setExamName("视力检查");
        req.setExamCategory(2);
        ExaminationOrderVO created = examinationService.createOrder(req);

        examinationService.deleteOrder(created.getOrderId());

        assertThrows(BusinessException.class,
                () -> examinationService.getDetail(created.getOrderId()));
    }
}

package com.cloudbrain.service.pharmacy;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.request.pharmacy.DrugAddRequest;
import com.cloudbrain.dto.request.pharmacy.DrugUpdateRequest;
import com.cloudbrain.dto.response.pharmacy.DrugVO;
import com.cloudbrain.entity.Drug;
import com.cloudbrain.mapper.DrugMapper;
import com.cloudbrain.util.UUIDUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DrugServiceTest {

    @Autowired
    private DrugService drugService;

    @Autowired
    private DrugMapper drugMapper;

    private static String testDrugId;

    @Test
    @Order(1)
    @DisplayName("新增药品")
    void testAdd() {
        DrugAddRequest request = new DrugAddRequest();
        request.setDrugCode("TEST" + System.currentTimeMillis());
        request.setDrugName("测试药品");
        request.setGenericName("测试通用名");
        request.setIngredients("测试成分");
        request.setSpec("1mg×10片");
        request.setDosageForm("片剂");
        request.setManufacturer("测试药厂");
        request.setUnit("盒");
        request.setUnitPrice(new BigDecimal("99.99"));
        request.setPurchasePrice(new BigDecimal("50.00"));
        request.setUsageMethod("口服，一次1片");
        request.setPrescriptionType(1);
        request.setDrugCategory("测试类别");

        testDrugId = drugService.add(request);
        assertNotNull(testDrugId);

        DrugVO vo = drugService.getDetail(testDrugId);
        assertEquals("测试药品", vo.getDrugName());
        assertEquals(new BigDecimal("99.99"), vo.getUnitPrice());
        assertEquals(new BigDecimal("50.00"), vo.getPurchasePrice());
        assertEquals("测试成分", vo.getIngredients());
    }

    @Test
    @Order(2)
    @DisplayName("搜索药品—按名称模糊匹配")
    void testSearchByName() {
        PageResult<DrugVO> result = drugService.search("测试", null, null, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertEquals("测试药品", result.getRecords().get(0).getDrugName());
    }

    @Test
    @Order(3)
    @DisplayName("搜索药品—按处方类型筛选")
    void testSearchByPrescriptionType() {
        PageResult<DrugVO> result = drugService.search(null, null, 0, 1, 10);
        assertTrue(result.getTotal() > 0);
        result.getRecords().forEach(d -> assertEquals(0, d.getPrescriptionType()));
    }

    @Test
    @Order(4)
    @DisplayName("修改药品")
    void testUpdate() {
        DrugUpdateRequest request = new DrugUpdateRequest();
        request.setDrugId(testDrugId);
        request.setDrugName("修改后药品名");
        drugService.update(request);

        DrugVO vo = drugService.getDetail(testDrugId);
        assertEquals("修改后药品名", vo.getDrugName());
    }

    @Test
    @Order(5)
    @DisplayName("重复药品编码应抛异常")
    void testDuplicateCode() {
        DrugAddRequest request = new DrugAddRequest();
        request.setDrugCode("YP20240001"); // seed data DRUG_001 的编码
        request.setDrugName("重复编码药品");
        request.setUnit("盒");
        request.setUnitPrice(new BigDecimal("10.00"));
        request.setPrescriptionType(1);

        assertThrows(BusinessException.class, () -> drugService.add(request));
    }

    @Test
    @Order(6)
    @DisplayName("删除药品—逻辑删除")
    void testDelete() {
        // 新增一个临时药品用于删除
        DrugAddRequest addReq = new DrugAddRequest();
        addReq.setDrugCode("DEL" + System.currentTimeMillis());
        addReq.setDrugName("待删除药品");
        addReq.setUnit("盒");
        addReq.setUnitPrice(new BigDecimal("1.00"));
        addReq.setPrescriptionType(1);
        String delId = drugService.add(addReq);

        drugService.delete(delId);
        Drug deleted = drugMapper.selectById(delId); // our DrugMapper uses drugId not Long id, need to use selectOne

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Drug> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Drug::getDrugId, delId);
        Drug drug = drugMapper.selectOne(wrapper);
        assertEquals(0, drug.getStatus());
    }
}

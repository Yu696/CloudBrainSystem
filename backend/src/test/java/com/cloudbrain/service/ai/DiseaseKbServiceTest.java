package com.cloudbrain.service.ai;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.DiseaseKnowledge;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 疾病知识库服务测试。
 * 所有写操作通过 @Transactional 自动回滚，不污染数据库。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class DiseaseKbServiceTest {

    @Autowired
    private DiseaseKbService diseaseKbService;

    private static String createdDiseaseId;

    // ======================== 查询 ========================

    @Test
    @Order(1)
    @DisplayName("按关键词搜索 — 搜索「糖尿病」")
    void testSearchByKeyword() {
        List<DiseaseKnowledge> list = diseaseKbService.searchByKeyword("糖尿病");
        assertNotNull(list);
        assertFalse(list.isEmpty(), "应搜索到糖尿病相关条目");
        list.forEach(d -> {
            boolean matchName = d.getDiseaseName() != null && d.getDiseaseName().contains("糖尿病");
            boolean matchSymptom = d.getSymptoms() != null && d.getSymptoms().contains("糖尿病");
            assertTrue(matchName || matchSymptom, "结果应匹配关键词");
        });
    }

    @Test
    @Order(2)
    @DisplayName("按关键词搜索 — 空关键词返回空列表")
    void testSearchByKeywordEmpty() {
        List<DiseaseKnowledge> list = diseaseKbService.searchByKeyword("");
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("按关键词搜索 — null 关键词返回空列表")
    void testSearchByKeywordNull() {
        List<DiseaseKnowledge> list = diseaseKbService.searchByKeyword(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("通用搜索 — 模糊匹配疾病名称")
    void testSearch() {
        List<DiseaseKnowledge> list = diseaseKbService.search("高血压");
        assertNotNull(list);
        assertFalse(list.isEmpty(), "应搜索到高血压相关条目");
    }

    @Test
    @Order(5)
    @DisplayName("列出所有启用的疾病条目")
    void testListAll() {
        List<DiseaseKnowledge> list = diseaseKbService.listAll();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "应有种子数据");
        list.forEach(d -> assertEquals(1, d.getStatus(), "仅列出启用条目"));
    }

    @Test
    @Order(6)
    @DisplayName("按ID获取 — 存在的条目")
    void testGetByDiseaseIdExisting() {
        // 先搜索一个已知存在的条目
        List<DiseaseKnowledge> all = diseaseKbService.listAll();
        assertFalse(all.isEmpty());
        String existingId = all.get(0).getDiseaseId();

        DiseaseKnowledge d = diseaseKbService.getByDiseaseId(existingId);
        assertNotNull(d);
        assertEquals(existingId, d.getDiseaseId());
    }

    @Test
    @Order(7)
    @DisplayName("按ID获取 — 不存在的条目抛异常")
    void testGetByDiseaseIdNotFound() {
        assertThrows(BusinessException.class,
                () -> diseaseKbService.getByDiseaseId("DISEASE_NOT_EXIST"));
    }

    // ======================== 新增 ========================

    @Test
    @Order(8)
    @DisplayName("新增疾病 — 正常创建")
    void testCreateSuccess() {
        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setDiseaseName("测试疾病_" + System.currentTimeMillis());
        d.setCategory("测试分类");
        d.setSymptoms("发热、咳嗽");
        d.setStatus(1);

        DiseaseKnowledge created = diseaseKbService.create(d);
        assertNotNull(created);
        assertNotNull(created.getDiseaseId());
        assertTrue(created.getDiseaseId().startsWith("DIS_"));
        assertEquals(d.getDiseaseName(), created.getDiseaseName());
        assertEquals(1, created.getStatus());
        createdDiseaseId = created.getDiseaseId();
    }

    @Test
    @Order(9)
    @DisplayName("新增疾病 — 疾病名称为空抛异常")
    void testCreateEmptyName() {
        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setDiseaseName("");

        assertThrows(BusinessException.class, () -> diseaseKbService.create(d));
    }

    @Test
    @Order(10)
    @DisplayName("新增疾病 — 重复名称抛异常")
    void testCreateDuplicateName() {
        List<DiseaseKnowledge> all = diseaseKbService.listAll();
        assertFalse(all.isEmpty());
        String existingName = all.get(0).getDiseaseName();

        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setDiseaseName(existingName);

        assertThrows(BusinessException.class, () -> diseaseKbService.create(d));
    }

    // ======================== 更新 ========================

    @Test
    @Order(11)
    @DisplayName("更新疾病 — 修改名称")
    void testUpdateSuccess() {
        // 先新增一个用于更新
        DiseaseKnowledge d = new DiseaseKnowledge();
        String newName = "待更新疾病_" + System.currentTimeMillis();
        d.setDiseaseName(newName);
        d.setCategory("更新前分类");
        DiseaseKnowledge created = diseaseKbService.create(d);

        // 更新
        DiseaseKnowledge update = new DiseaseKnowledge();
        update.setDiseaseId(created.getDiseaseId());
        update.setDiseaseName("已更新疾病_" + System.currentTimeMillis());
        update.setCategory("更新后分类");
        DiseaseKnowledge updated = diseaseKbService.update(update);

        assertEquals(update.getDiseaseName(), updated.getDiseaseName());
        assertEquals("更新后分类", updated.getCategory());
    }

    @Test
    @Order(12)
    @DisplayName("更新疾病 — 不存在的ID抛异常")
    void testUpdateNotFound() {
        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setDiseaseId("DISEASE_NOT_EXIST");
        d.setDiseaseName("不存在的疾病");

        assertThrows(BusinessException.class, () -> diseaseKbService.update(d));
    }

    @Test
    @Order(13)
    @DisplayName("更新疾病 — ID为空抛异常")
    void testUpdateNullId() {
        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setDiseaseName("无ID疾病");

        assertThrows(BusinessException.class, () -> diseaseKbService.update(d));
    }

    // ======================== 删除 ========================

    @Test
    @Order(14)
    @DisplayName("删除疾病 — 正常删除")
    void testDeleteSuccess() {
        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setDiseaseName("待删除疾病_" + System.currentTimeMillis());
        DiseaseKnowledge created = diseaseKbService.create(d);

        // 不应抛异常
        assertDoesNotThrow(() -> diseaseKbService.delete(created.getDiseaseId()));

        // 删除后再次查询应抛异常
        assertThrows(BusinessException.class,
                () -> diseaseKbService.getByDiseaseId(created.getDiseaseId()));
    }

    @Test
    @Order(15)
    @DisplayName("删除疾病 — 不存在的ID抛异常")
    void testDeleteNotFound() {
        assertThrows(BusinessException.class,
                () -> diseaseKbService.delete("DISEASE_NOT_EXIST"));
    }
}

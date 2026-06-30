package com.cloudbrain.service.ai;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.PromptTemplate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prompt 模板服务测试。
 * 所有写操作通过 @Transactional 自动回滚，不污染数据库。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class PromptTemplateServiceTest {

    @Autowired
    private PromptTemplateService promptTemplateService;

    private static String createdTemplateId;

    // ======================== 查询 ========================

    @Test
    @Order(1)
    @DisplayName("按类型列出 — 分诊模板(type=0)")
    void testListByType() {
        List<PromptTemplate> list = promptTemplateService.listByType(0);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "分诊模板应有种子数据");
        list.forEach(t -> assertEquals(0, t.getTemplateType()));
    }

    @Test
    @Order(2)
    @DisplayName("按类型列出 — null 类型返回全部")
    void testListByTypeNull() {
        List<PromptTemplate> list = promptTemplateService.listByType(null);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("列出全部模板")
    void testListAll() {
        List<PromptTemplate> list = promptTemplateService.listAll();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "应有种子数据");
    }

    @Test
    @Order(4)
    @DisplayName("按ID获取 — 存在的模板")
    void testGetByTemplateIdExisting() {
        List<PromptTemplate> all = promptTemplateService.listAll();
        assertFalse(all.isEmpty());
        String existingId = all.get(0).getTemplateId();

        PromptTemplate t = promptTemplateService.getByTemplateId(existingId);
        assertNotNull(t);
        assertEquals(existingId, t.getTemplateId());
    }

    @Test
    @Order(5)
    @DisplayName("按ID获取 — 不存在的模板抛异常")
    void testGetByTemplateIdNotFound() {
        assertThrows(BusinessException.class,
                () -> promptTemplateService.getByTemplateId("TPL_NOT_EXIST"));
    }

    // ======================== 新增 ========================

    @Test
    @Order(6)
    @DisplayName("创建模板 — 正常创建")
    void testCreateSuccess() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("测试模板_" + System.currentTimeMillis());
        t.setTemplateType(0);
        t.setContent("患者主诉：{{chief_complaint}}\n体温：{{temperature}}℃");
        t.setVariables("[\"chief_complaint\", \"temperature\"]");

        PromptTemplate created = promptTemplateService.create(t);
        assertNotNull(created.getTemplateId());
        assertTrue(created.getTemplateId().startsWith("TPL_"));
        assertEquals(1, created.getVersion());
        assertEquals(1, created.getStatus());
        createdTemplateId = created.getTemplateId();
    }

    @Test
    @Order(7)
    @DisplayName("创建模板 — 名称为空抛异常")
    void testCreateEmptyName() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("");
        t.setTemplateType(0);
        t.setContent("content");

        assertThrows(BusinessException.class, () -> promptTemplateService.create(t));
    }

    @Test
    @Order(8)
    @DisplayName("创建模板 — 类型为空抛异常")
    void testCreateNullType() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("无类型模板");
        t.setContent("content");

        assertThrows(BusinessException.class, () -> promptTemplateService.create(t));
    }

    @Test
    @Order(9)
    @DisplayName("创建模板 — 内容为空抛异常")
    void testCreateEmptyContent() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("空内容模板");
        t.setTemplateType(0);
        t.setContent("");

        assertThrows(BusinessException.class, () -> promptTemplateService.create(t));
    }

    // ======================== 更新 ========================

    @Test
    @Order(10)
    @DisplayName("更新模板 — 修改内容后版本号+1")
    void testUpdateIncrementsVersion() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("版本测试_" + System.currentTimeMillis());
        t.setTemplateType(0);
        t.setContent("初始内容");
        PromptTemplate created = promptTemplateService.create(t);
        assertEquals(1, created.getVersion());

        PromptTemplate update = new PromptTemplate();
        update.setTemplateId(created.getTemplateId());
        update.setContent("更新后的内容");
        PromptTemplate updated = promptTemplateService.update(update);
        assertEquals(2, updated.getVersion());
        assertEquals("更新后的内容", updated.getContent());
    }

    @Test
    @Order(11)
    @DisplayName("更新模板 — 不存在的ID抛异常")
    void testUpdateNotFound() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateId("TPL_NOT_EXIST");
        t.setTemplateName("不存在的模板");

        assertThrows(BusinessException.class, () -> promptTemplateService.update(t));
    }

    // ======================== 状态管理 ========================

    @Test
    @Order(12)
    @DisplayName("更新状态 — 正常启用/禁用")
    void testUpdateStatus() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("状态测试_" + System.currentTimeMillis());
        t.setTemplateType(0);
        t.setContent("content");
        PromptTemplate created = promptTemplateService.create(t);

        // 禁用
        promptTemplateService.updateStatus(created.getTemplateId(), 0);
        PromptTemplate disabled = promptTemplateService.getByTemplateId(created.getTemplateId());
        assertEquals(0, disabled.getStatus());

        // 启用
        promptTemplateService.updateStatus(created.getTemplateId(), 1);
        PromptTemplate enabled = promptTemplateService.getByTemplateId(created.getTemplateId());
        assertEquals(1, enabled.getStatus());
    }

    @Test
    @Order(13)
    @DisplayName("更新状态 — 无效状态值抛异常")
    void testUpdateStatusInvalid() {
        List<PromptTemplate> all = promptTemplateService.listAll();
        assertFalse(all.isEmpty(), "需要种子数据");
        String id = all.get(0).getTemplateId();

        assertThrows(BusinessException.class,
                () -> promptTemplateService.updateStatus(id, 2));
        assertThrows(BusinessException.class,
                () -> promptTemplateService.updateStatus(id, null));
    }

    // ======================== 删除 ========================

    @Test
    @Order(14)
    @DisplayName("删除模板 — 正常删除")
    void testDeleteSuccess() {
        PromptTemplate t = new PromptTemplate();
        t.setTemplateName("待删除模板_" + System.currentTimeMillis());
        t.setTemplateType(0);
        t.setContent("content");
        PromptTemplate created = promptTemplateService.create(t);

        assertDoesNotThrow(() -> promptTemplateService.delete(created.getTemplateId()));
        assertThrows(BusinessException.class,
                () -> promptTemplateService.getByTemplateId(created.getTemplateId()));
    }

    @Test
    @Order(15)
    @DisplayName("删除模板 — 不存在的ID抛异常")
    void testDeleteNotFound() {
        assertThrows(BusinessException.class,
                () -> promptTemplateService.delete("TPL_NOT_EXIST"));
    }

    // ======================== render ========================

    @Test
    @Order(16)
    @DisplayName("渲染模板 — 所有变量替换成功")
    void testRenderAllVariablesReplaced() {
        String template = "患者{{name}}，年龄{{age}}岁，主诉：{{complaint}}";
        Map<String, String> vars = Map.of("name", "张三", "age", "30", "complaint", "头痛三天");
        String result = promptTemplateService.render(template, vars);
        assertEquals("患者张三，年龄30岁，主诉：头痛三天", result);
    }

    @Test
    @Order(17)
    @DisplayName("渲染模板 — 未匹配的变量被清除")
    void testRenderUnmatchedVariablesCleaned() {
        String template = "患者{{name}}，诊断：{{diagnosis}}，注意{{warning}}";
        Map<String, String> vars = Map.of("name", "张三");
        String result = promptTemplateService.render(template, vars);
        assertTrue(result.contains("张三"));
        assertFalse(result.contains("{{"), "未匹配的占位符应被清除");
        assertFalse(result.contains("diagnosis"));
    }

    @Test
    @Order(18)
    @DisplayName("渲染模板 — 特殊字符转义")
    void testRenderSpecialCharsEscaped() {
        String template = "{{value}}";
        Map<String, String> vars = Map.of("value", "test\\value\"quote");
        String result = promptTemplateService.render(template, vars);
        assertFalse(result.contains("\""), "双引号应被转义");
    }

    @Test
    @Order(19)
    @DisplayName("渲染模板 — 无占位符直接返回原内容")
    void testRenderNoPlaceholders() {
        String template = "这是纯文本，无变量";
        String result = promptTemplateService.render(template, Map.of());
        assertEquals("这是纯文本，无变量", result);
    }
}

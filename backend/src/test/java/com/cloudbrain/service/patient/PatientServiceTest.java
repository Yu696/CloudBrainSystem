package com.cloudbrain.service.patient;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PatientCreateRequest;
import com.cloudbrain.dto.request.PatientUpdateRequest;
import com.cloudbrain.dto.response.PatientCreateVO;
import com.cloudbrain.dto.response.PatientInfoVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 患者档案服务测试。
 * 所有写操作通过 @Transactional 自动回滚，不污染数据库。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class PatientServiceTest {

    @Autowired
    private PatientService patientService;

    private static final String EXISTING_PATIENT = "PAT_001";  // 张三
    private static final String EXISTING_PHONE = "13900000101";
    private static final String EXISTING_ID_CARD = "110101199001011234";

    private static String createdPatientId;

    // ======================== 创建患者 ========================

    @Test
    @Order(1)
    @DisplayName("创建患者 — 正常创建")
    void testCreateSuccess() {
        PatientCreateRequest req = new PatientCreateRequest();
        req.setName("测试患者_" + System.currentTimeMillis());
        req.setIdCard("420101" + String.format("%012d", System.currentTimeMillis() % 1000000000000L));
        req.setPhone("155" + String.format("%08d", System.currentTimeMillis() % 100000000));
        req.setGender(1);
        req.setBirthDate(LocalDate.of(1990, 1, 1));
        req.setSource(1);

        PatientCreateVO vo = patientService.createPatient(req);
        assertNotNull(vo);
        assertNotNull(vo.getPatientId());
        assertTrue(vo.getPatientId().startsWith("PAT_"));
        assertNotNull(vo.getMedicalRecordNo());
        assertTrue(vo.getMedicalRecordNo().startsWith("MR"));
        createdPatientId = vo.getPatientId();
    }

    @Test
    @Order(2)
    @DisplayName("创建患者 — 重复身份证号抛异常")
    void testCreateDuplicateIdCard() {
        PatientCreateRequest req = new PatientCreateRequest();
        req.setName("重复身份证患者");
        req.setIdCard(EXISTING_ID_CARD);
        req.setPhone("15500000001");
        req.setGender(1);
        req.setBirthDate(LocalDate.of(1990, 1, 1));

        assertThrows(BusinessException.class, () -> patientService.createPatient(req));
    }

    @Test
    @Order(3)
    @DisplayName("创建患者 — 重复手机号抛异常")
    void testCreateDuplicatePhone() {
        PatientCreateRequest req = new PatientCreateRequest();
        req.setName("重复手机患者");
        req.setIdCard("420101199001019999");
        req.setPhone(EXISTING_PHONE);
        req.setGender(1);
        req.setBirthDate(LocalDate.of(1990, 1, 1));

        assertThrows(BusinessException.class, () -> patientService.createPatient(req));
    }

    // ======================== 查询患者 ========================

    @Test
    @Order(4)
    @DisplayName("查询患者信息 — 存在的患者")
    void testGetPatientInfoExisting() {
        PatientInfoVO vo = patientService.getPatientInfo(EXISTING_PATIENT);
        assertNotNull(vo);
        assertEquals("张三", vo.getName());
        assertEquals(EXISTING_PHONE, vo.getPhone());
    }

    @Test
    @Order(5)
    @DisplayName("查询患者信息 — 不存在的患者抛异常")
    void testGetPatientInfoNotFound() {
        assertThrows(BusinessException.class,
                () -> patientService.getPatientInfo("PAT_NOT_EXIST"));
    }

    @Test
    @Order(6)
    @DisplayName("患者列表 — 按姓名查询")
    void testListByName() {
        List<PatientInfoVO> list = patientService.listPatients("张三", null, null);
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(p -> "张三".equals(p.getName())));
    }

    @Test
    @Order(7)
    @DisplayName("患者列表 — 按手机号查询")
    void testListByPhone() {
        List<PatientInfoVO> list = patientService.listPatients(null, EXISTING_PHONE, null);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("张三", list.get(0).getName());
    }

    @Test
    @Order(8)
    @DisplayName("患者列表 — 无匹配结果返回空列表")
    void testListNoMatch() {
        List<PatientInfoVO> list = patientService.listPatients("不存在的姓名XYZ123", null, null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // ======================== 更新患者 ========================

    @Test
    @Order(9)
    @DisplayName("更新患者 — 正常更新姓名")
    void testUpdateSuccess() {
        // 创建一个患者用于更新
        PatientCreateRequest createReq = new PatientCreateRequest();
        createReq.setName("待更新患者_" + System.currentTimeMillis());
        createReq.setIdCard("420101" + String.format("%012d", (System.currentTimeMillis() % 1000000000000L) + 1));
        createReq.setPhone("156" + String.format("%08d", (System.currentTimeMillis() % 100000000) + 1));
        createReq.setGender(1);
        createReq.setBirthDate(LocalDate.of(1990, 1, 1));
        PatientCreateVO created = patientService.createPatient(createReq);

        PatientUpdateRequest update = new PatientUpdateRequest();
        update.setPatientId(created.getPatientId());
        update.setName("已更新姓名_" + System.currentTimeMillis());

        patientService.updatePatient(update);
        PatientInfoVO vo = patientService.getPatientInfo(created.getPatientId());
        assertEquals(update.getName(), vo.getName());
    }

    @Test
    @Order(10)
    @DisplayName("更新患者 — 手机号冲突抛异常")
    void testUpdatePhoneConflict() {
        PatientCreateRequest createReq = new PatientCreateRequest();
        createReq.setName("冲突测试患者_" + System.currentTimeMillis());
        createReq.setIdCard("420101" + String.format("%012d", (System.currentTimeMillis() % 1000000000000L) + 2));
        createReq.setPhone("157" + String.format("%08d", (System.currentTimeMillis() % 100000000) + 2));
        createReq.setGender(1);
        createReq.setBirthDate(LocalDate.of(1990, 1, 1));
        PatientCreateVO created = patientService.createPatient(createReq);

        PatientUpdateRequest update = new PatientUpdateRequest();
        update.setPatientId(created.getPatientId());
        update.setPhone(EXISTING_PHONE); // PAT_001 的手机号

        assertThrows(BusinessException.class, () -> patientService.updatePatient(update));
    }

    @Test
    @Order(11)
    @DisplayName("更新患者 — 不存在的患者抛异常")
    void testUpdateNotFound() {
        PatientUpdateRequest req = new PatientUpdateRequest();
        req.setPatientId("PAT_NOT_EXIST");
        req.setName("不存在的患者");

        assertThrows(BusinessException.class, () -> patientService.updatePatient(req));
    }

    // ======================== 身份证校验 ========================

    @Test
    @Order(12)
    @DisplayName("身份证校验 — 已存在的身份证")
    void testCheckIdCardExists() {
        assertTrue(patientService.checkIdCard(EXISTING_ID_CARD));
    }

    @Test
    @Order(13)
    @DisplayName("身份证校验 — 不存在的身份证")
    void testCheckIdCardNotExists() {
        assertFalse(patientService.checkIdCard("000000000000000000"));
    }

    // ======================== 按用户ID查找 ========================

    @Test
    @Order(14)
    @DisplayName("按用户ID查找 — 存在的用户")
    void testFindByUserIdExisting() {
        PatientInfoVO vo = patientService.findByUserId("USR_pat001");
        assertNotNull(vo);
        assertEquals("张三", vo.getName());
    }

    @Test
    @Order(15)
    @DisplayName("按用户ID查找 — 不存在的用户抛异常")
    void testFindByUserIdNotFound() {
        assertThrows(BusinessException.class,
                () -> patientService.findByUserId("USR_NOT_EXIST"));
    }

    // ======================== 医生查看患者 ========================

    @Test
    @Order(16)
    @DisplayName("医生患者列表 — 有预约记录")
    void testListPatientsByDoctorHasPatients() {
        List<PatientInfoVO> list = patientService.listPatientsByDoctor("DOC_001", null, null, null);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "DOC_001 应有患者预约记录");
    }

    @Test
    @Order(17)
    @DisplayName("医生患者列表 — 无预约记录的医生返回空列表")
    void testListPatientsByDoctorNoAppointments() {
        List<PatientInfoVO> list = patientService.listPatientsByDoctor("DOC_999", null, null, null);
        assertNotNull(list);
        assertTrue(list.isEmpty(), "DOC_999 不应有预约记录");
    }
}

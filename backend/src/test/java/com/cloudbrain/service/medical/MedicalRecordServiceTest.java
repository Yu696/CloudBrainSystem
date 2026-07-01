package com.cloudbrain.service.medical;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.MedicalRecordCreateRequest;
import com.cloudbrain.dto.request.MedicalRecordUpdateRequest;
import com.cloudbrain.dto.response.MedicalRecordVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 病历服务测试。
 * 所有写操作通过 @Transactional 自动回滚，不污染数据库。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class MedicalRecordServiceTest {

    @Autowired
    private MedicalRecordService medicalRecordService;

    private static final String PATIENT_ID = "PAT_001";
    private static final String DOCTOR_ID = "DOC_001";
    private static final String EXISTING_RECORD = "REC_test001";

    // ======================== 创建病历 ========================

    @Test
    @Order(1)
    @DisplayName("创建病历 — 无预约ID正常创建")
    void testCreateRecordNew() {
        MedicalRecordCreateRequest req = new MedicalRecordCreateRequest();
        req.setPatientId(PATIENT_ID);
        req.setDoctorId(DOCTOR_ID);
        req.setChiefComplaint("测试主诉_" + System.currentTimeMillis());
        req.setDiagnosis("测试诊断");

        MedicalRecordVO vo = medicalRecordService.createRecord(req);
        assertNotNull(vo);
        assertNotNull(vo.getRecordId());
        assertTrue(vo.getRecordId().startsWith("REC_"));
        assertEquals(0, vo.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("创建病历 — 已有预约的病历会更新而非新建")
    void testCreateRecordExistingAppointment() {
        // REC_test001 关联的预约是 APT_test001
        MedicalRecordCreateRequest req = new MedicalRecordCreateRequest();
        req.setPatientId(PATIENT_ID);
        req.setDoctorId(DOCTOR_ID);
        req.setAppointmentId("APT_test001");
        req.setChiefComplaint("更新后的主诉_" + System.currentTimeMillis());
        req.setDiagnosis("更新后的诊断");

        MedicalRecordVO vo = medicalRecordService.createRecord(req);
        assertEquals(EXISTING_RECORD, vo.getRecordId(), "应更新已有病历而非新建");
    }

    // ======================== 查询病历 ========================

    @Test
    @Order(3)
    @DisplayName("病历列表 — 按患者ID查询")
    void testListRecordsByPatient() {
        List<MedicalRecordVO> list = medicalRecordService.listRecords(PATIENT_ID, null);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "PAT_001 应有病历");
        assertTrue(list.stream().anyMatch(r -> EXISTING_RECORD.equals(r.getRecordId())));
    }

    @Test
    @Order(4)
    @DisplayName("病历列表 — 按医生ID查询")
    void testListRecordsByDoctor() {
        List<MedicalRecordVO> list = medicalRecordService.listRecords(null, DOCTOR_ID);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "DOC_001 应有病历");
    }

    @Test
    @Order(5)
    @DisplayName("病历列表 — 同时按患者和医生查询")
    void testListRecordsBoth() {
        List<MedicalRecordVO> list = medicalRecordService.listRecords(PATIENT_ID, DOCTOR_ID);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("病历详情 — 存在的病历")
    void testGetRecordDetailExisting() {
        MedicalRecordVO vo = medicalRecordService.getRecordDetail(EXISTING_RECORD);
        assertNotNull(vo);
        assertEquals(EXISTING_RECORD, vo.getRecordId());
        assertNotNull(vo.getChiefComplaint());
    }

    @Test
    @Order(7)
    @DisplayName("病历详情 — 不存在的病历抛异常")
    void testGetRecordDetailNotFound() {
        assertThrows(BusinessException.class,
                () -> medicalRecordService.getRecordDetail("REC_NOT_EXIST"));
    }

    // ======================== 更新病历 ========================

    @Test
    @Order(8)
    @DisplayName("更新病历 — 正常更新主诉")
    void testUpdateRecordSuccess() {
        String newComplaint = "更新后的主诉_" + System.currentTimeMillis();
        MedicalRecordUpdateRequest req = new MedicalRecordUpdateRequest();
        req.setRecordId(EXISTING_RECORD);
        req.setChiefComplaint(newComplaint);

        medicalRecordService.updateRecord(req);

        MedicalRecordVO vo = medicalRecordService.getRecordDetail(EXISTING_RECORD);
        assertEquals(newComplaint, vo.getChiefComplaint());
    }

    @Test
    @Order(9)
    @DisplayName("更新病历 — 不存在的病历抛异常")
    void testUpdateRecordNotFound() {
        MedicalRecordUpdateRequest req = new MedicalRecordUpdateRequest();
        req.setRecordId("REC_NOT_EXIST");
        req.setChiefComplaint("测试");

        assertThrows(BusinessException.class, () -> medicalRecordService.updateRecord(req));
    }

    // ======================== 完成病历 ========================

    @Test
    @Order(10)
    @DisplayName("完成病历 — 状态变更为已完成")
    void testCompleteRecord() {
        // 新建一个草稿病历用于完成
        MedicalRecordCreateRequest createReq = new MedicalRecordCreateRequest();
        createReq.setPatientId(PATIENT_ID);
        createReq.setDoctorId(DOCTOR_ID);
        createReq.setChiefComplaint("待完成病历");
        MedicalRecordVO created = medicalRecordService.createRecord(createReq);
        assertEquals(0, created.getStatus());

        medicalRecordService.completeRecord(created.getRecordId());

        MedicalRecordVO completed = medicalRecordService.getRecordDetail(created.getRecordId());
        assertEquals(1, completed.getStatus(), "完成后的病历状态应为已完成");
    }

    @Test
    @Order(11)
    @DisplayName("完成病历 — 不存在的病历抛异常")
    void testCompleteRecordNotFound() {
        assertThrows(BusinessException.class,
                () -> medicalRecordService.completeRecord("REC_NOT_EXIST"));
    }

    // ======================== 删除病历 ========================

    @Test
    @Order(12)
    @DisplayName("删除病历 — 新建后删除")
    void testDeleteRecord() {
        MedicalRecordCreateRequest createReq = new MedicalRecordCreateRequest();
        createReq.setPatientId(PATIENT_ID);
        createReq.setDoctorId(DOCTOR_ID);
        createReq.setChiefComplaint("待删除病历");
        MedicalRecordVO created = medicalRecordService.createRecord(createReq);

        medicalRecordService.deleteRecord(created.getRecordId());

        assertThrows(BusinessException.class,
                () -> medicalRecordService.getRecordDetail(created.getRecordId()));
    }

    @Test
    @Order(13)
    @DisplayName("删除病历 — 不存在的病历抛异常")
    void testDeleteRecordNotFound() {
        assertThrows(BusinessException.class,
                () -> medicalRecordService.deleteRecord("REC_NOT_EXIST"));
    }
}

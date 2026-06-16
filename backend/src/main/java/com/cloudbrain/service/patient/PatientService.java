package com.cloudbrain.service.patient;

import com.cloudbrain.dto.request.PatientCreateRequest;
import com.cloudbrain.dto.request.PatientUpdateRequest;
import com.cloudbrain.dto.response.PatientCreateVO;
import com.cloudbrain.dto.response.PatientInfoVO;

import java.util.List;

public interface PatientService {

    /** 创建患者档案，生成病历号，校验身份证唯一性 */
    PatientCreateVO createPatient(PatientCreateRequest request);

    /** 查询患者详细信息 */
    PatientInfoVO getPatientInfo(String patientId);

    /** 根据姓名、手机号、病历号搜索患者列表 */
    List<PatientInfoVO> listPatients(String name, String phone, String medicalRecordNo);

    /** 更新患者档案信息 */
    void updatePatient(PatientUpdateRequest request);

    /** 校验身份证号是否已存在 */
    boolean checkIdCard(String idCard);
}

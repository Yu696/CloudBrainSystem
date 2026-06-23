package com.cloudbrain.service.medical;

import com.cloudbrain.dto.request.MedicalRecordCreateRequest;
import com.cloudbrain.dto.request.MedicalRecordUpdateRequest;
import com.cloudbrain.dto.response.MedicalRecordVO;

import java.util.List;

public interface MedicalRecordService {

    /** 创建病历 */
    MedicalRecordVO createRecord(MedicalRecordCreateRequest request);

    /** 查询病历列表（按患者或医生筛选） */
    List<MedicalRecordVO> listRecords(String patientId, String doctorId);

    /** 查询病历详情 */
    MedicalRecordVO getRecordDetail(String recordId);

    /** 更新病历 */
    void updateRecord(MedicalRecordUpdateRequest request);

    /** 完成病历 */
    void completeRecord(String recordId);

    /** 删除病历 */
    void deleteRecord(String recordId);
}

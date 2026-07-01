package com.cloudbrain.service.medical;

import com.cloudbrain.dto.request.PrescriptionCreateRequest;
import com.cloudbrain.dto.response.PrescriptionVO;

import java.util.List;

public interface PrescriptionService {

    /** 开具处方 */
    PrescriptionVO createPrescription(PrescriptionCreateRequest request);

    /** 更新处方 */
    PrescriptionVO updatePrescription(String prescriptionId, PrescriptionCreateRequest request);

    /** 删除处方 */
    void deletePrescription(String prescriptionId);

    /** 处方列表 */
    List<PrescriptionVO> listPrescriptions(String recordId);

    /** 处方详情（含明细） */
    PrescriptionVO getPrescriptionDetail(String prescriptionId);

    /** 审核处方（待审核→已审核） */
    void auditPrescription(String prescriptionId);

    /** 支付处方费用（钱包扣款） */
    void payOrder(String prescriptionId);
}

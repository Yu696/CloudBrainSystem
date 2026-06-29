package com.cloudbrain.service.medical;

import com.cloudbrain.dto.request.ExaminationOrderCreateRequest;
import com.cloudbrain.dto.request.ExaminationResultCreateRequest;
import com.cloudbrain.dto.response.ExaminationOrderVO;
import com.cloudbrain.dto.response.ExaminationResultVO;

import java.util.List;

public interface ExaminationService {

    /** 开检查单 */
    ExaminationOrderVO createOrder(ExaminationOrderCreateRequest request);

    /** 更新检查单 */
    ExaminationOrderVO updateOrder(String orderId, ExaminationOrderCreateRequest request);

    /** 删除检查单 */
    void deleteOrder(String orderId);

    /** 检查单列表 */
    List<ExaminationOrderVO> listOrders(String recordId);

    /** 检查结果 */
    ExaminationResultVO getResult(String orderId);

    /** 影像检查单列表（医生端：查询需要上传影像的检查单） */
    List<ExaminationOrderVO> listImagingOrders(String doctorId);

    /** 保存检查结果（新增或更新） */
    ExaminationResultVO saveResult(ExaminationResultCreateRequest request);
}

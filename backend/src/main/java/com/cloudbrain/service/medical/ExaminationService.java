package com.cloudbrain.service.medical;

import com.cloudbrain.dto.request.ExaminationOrderCreateRequest;
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
}

package com.cloudbrain.service.pharmacy;

import com.cloudbrain.dto.request.pharmacy.DispenseRequest;
import com.cloudbrain.dto.response.pharmacy.DispenseResultVO;

public interface DispenseService {

    /** 发药出库 */
    DispenseResultVO dispense(DispenseRequest request);
}

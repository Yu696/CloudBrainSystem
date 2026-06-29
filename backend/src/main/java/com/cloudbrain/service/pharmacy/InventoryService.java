package com.cloudbrain.service.pharmacy;

import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;

import java.util.List;

public interface InventoryService {

    /** 查询药品库存 */
    StockVO getStock(String drugId);

    /** 库存预警列表 */
    List<StockAlertVO> listAlerts(Integer type);

    /** 标记预警已处理 */
    void handleAlert(Long alertId);

    /** 根据仓库查询库存列表 */
    PageResult<StockVO> listByWarehouse(String warehouseId, int page, int pageSize);
}

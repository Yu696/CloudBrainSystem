package com.cloudbrain.service.pharmacy;

import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    /** 查询药品库存 */
    StockVO getStock(String drugId);

    /** 库存预警列表 */
    List<StockAlertVO> listAlerts(Integer type);

    /** 标记预警已处理 */
    void handleAlert(Long alertId);

    /** 根据仓库查询库存列表 */
    PageResult<StockVO> listByWarehouse(String warehouseId, String keyword, int page, int pageSize);

    /** 手动调整库存（正数入库，负数出库） */
    void adjustStock(String drugId, Integer quantity, String warehouseId, String batchNo,
                     LocalDate productionDate, LocalDate expiryDate, Integer minStock, Integer maxStock);

    /** 销毁过期药品（库存置 0） */
    void destroyExpired(String drugId, String warehouseId, String batchNo);

    /** 删除预警记录 */
    void deleteAlert(Long alertId);

    /** 库存转移（同药品在不同仓库间调拨） */
    void transferStock(String drugId, String fromWarehouseId, String toWarehouseId, Integer quantity, String batchNo);
}

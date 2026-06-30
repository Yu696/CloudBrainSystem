package com.cloudbrain.service.pharmacy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;
import com.cloudbrain.entity.Drug;
import com.cloudbrain.entity.DrugStock;
import com.cloudbrain.entity.StockAlert;
import com.cloudbrain.entity.Warehouse;
import com.cloudbrain.mapper.DrugMapper;
import com.cloudbrain.mapper.DrugStockMapper;
import com.cloudbrain.mapper.StockAlertMapper;
import com.cloudbrain.mapper.WarehouseMapper;
import com.cloudbrain.service.pharmacy.InventoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final DrugStockMapper drugStockMapper;
    private final DrugMapper drugMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockAlertMapper stockAlertMapper;

    // ==================== 查询 ====================

    @Override
    public StockVO getStock(String drugId) {
        List<DrugStock> stocks = drugStockMapper.selectList(
                new LambdaQueryWrapper<DrugStock>().eq(DrugStock::getDrugId, drugId));
        if (stocks.isEmpty()) {
            throw new BusinessException("该药品暂无库存记录");
        }
        DrugStock stock = stocks.get(0);

        Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, drugId));
        String warehouseName = null;
        if (stock.getWarehouseId() != null) {
            Warehouse wh = warehouseMapper.selectOne(
                    new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getWarehouseId, stock.getWarehouseId()));
            if (wh != null) warehouseName = wh.getName();
        }

        return StockVO.builder()
                .drugId(stock.getDrugId())
                .drugName(drug != null ? drug.getDrugName() : null)
                .warehouseId(stock.getWarehouseId())
                .warehouseName(warehouseName)
                .currentStock(stock.getCurrentStock())
                .minStock(stock.getMinStock())
                .maxStock(stock.getMaxStock())
                .batchNo(stock.getBatchNo())
                .productionDate(stock.getProductionDate())
                .expiryDate(stock.getExpiryDate())
                .status(stock.getStatus())
                .createTime(stock.getCreateTime())
                .updateTime(stock.getUpdateTime())
                .build();
    }

    @Override
    public PageResult<StockVO> listByWarehouse(String warehouseId, String keyword, int page, int pageSize) {
        LambdaQueryWrapper<DrugStock> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null && !warehouseId.isBlank()) {
            wrapper.eq(DrugStock::getWarehouseId, warehouseId);
        }
        if (keyword != null && !keyword.isBlank()) {
            List<Drug> matchedDrugs = drugMapper.selectList(
                    new LambdaQueryWrapper<Drug>()
                            .like(Drug::getDrugId, keyword)
                            .or().like(Drug::getDrugName, keyword)
                            .or().like(Drug::getDrugCode, keyword));
            if (matchedDrugs.isEmpty()) {
                return PageResult.of(0, page, pageSize, List.of());
            }
            wrapper.in(DrugStock::getDrugId, matchedDrugs.stream().map(Drug::getDrugId).toList());
        }
        wrapper.orderByDesc(DrugStock::getUpdateTime);

        Page<DrugStock> result = drugStockMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<StockVO> records = new ArrayList<>();
        for (DrugStock stock : result.getRecords()) {
            Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, stock.getDrugId()));
            String warehouseName = null;
            if (stock.getWarehouseId() != null) {
                Warehouse wh = warehouseMapper.selectOne(
                        new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getWarehouseId, stock.getWarehouseId()));
                if (wh != null) warehouseName = wh.getName();
            }
            records.add(StockVO.builder()
                    .drugId(stock.getDrugId())
                    .drugName(drug != null ? drug.getDrugName() : null)
                    .warehouseId(stock.getWarehouseId())
                    .warehouseName(warehouseName)
                    .currentStock(stock.getCurrentStock())
                    .minStock(stock.getMinStock())
                    .maxStock(stock.getMaxStock())
                    .batchNo(stock.getBatchNo())
                    .productionDate(stock.getProductionDate())
                    .expiryDate(stock.getExpiryDate())
                    .status(stock.getStatus())
                    .createTime(stock.getCreateTime())
                    .updateTime(stock.getUpdateTime())
                    .build());
        }
        return PageResult.of(result.getTotal(), page, pageSize, records);
    }

    // ==================== 库存预警列表 ====================

    @Override
    public List<StockAlertVO> listAlerts(Integer type) {
        LambdaQueryWrapper<StockAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockAlert::getIsHandled, 0);
        if (type != null) {
            wrapper.eq(StockAlert::getAlertType, type);
        }
        wrapper.orderByDesc(StockAlert::getCreateTime);

        List<StockAlert> alerts = stockAlertMapper.selectList(wrapper);

        // 查 drug_stock 中已过期的批次
        LocalDate today = LocalDate.now();
        List<DrugStock> expiredStocks = drugStockMapper.selectList(
                new LambdaQueryWrapper<DrugStock>()
                        .lt(DrugStock::getExpiryDate, today)
                        .eq(DrugStock::getStatus, 1)
                        .gt(DrugStock::getCurrentStock, 0));

        // 标记已在 stock_alert 中有过期预警的 (drug_id, warehouse_id, batch_no)
        Set<String> alertedKeys = alerts.stream()
                .filter(a -> a.getAlertType() == 1)
                .map(a -> a.getDrugId() + "|" + (a.getWarehouseId() != null ? a.getWarehouseId() : "") + "|" + (a.getBatchNo() != null ? a.getBatchNo() : ""))
                .collect(Collectors.toSet());

        List<StockAlertVO> result = new ArrayList<>();
        for (StockAlert alert : alerts) {
            Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, alert.getDrugId()));
            Integer realStock = alert.getAlertType() == 1
                    ? getTotalStock(alert.getDrugId(), alert.getWarehouseId())
                    : getBatchStock(alert.getDrugId(), alert.getWarehouseId(), alert.getBatchNo());
            String warehouseName = getWarehouseName(alert.getWarehouseId());
            result.add(StockAlertVO.builder()
                    .id(alert.getId())
                    .drugId(alert.getDrugId())
                    .drugName(drug != null ? drug.getDrugName() : null)
                    .warehouseId(alert.getWarehouseId())
                    .warehouseName(warehouseName)
                    .batchNo(alert.getBatchNo())
                    .alertType(alert.getAlertType())
                    .alertTypeName(StockAlertVO.alertTypeName(alert.getAlertType()))
                    .currentStock(realStock)
                    .threshold(alert.getThreshold())
                    .alertMessage(alert.getAlertMessage())
                    .isHandled(alert.getIsHandled() != null && alert.getIsHandled() == 1)
                    .handledBy(alert.getHandledBy())
                    .handleTime(alert.getHandleTime())
                    .createTime(alert.getCreateTime())
                    .build());
        }
        // 补充 drug_stock 中已过期但 stock_alert 中无记录的批次
        if (type == null || type == 1) {
            for (DrugStock es : expiredStocks) {
                String key = es.getDrugId() + "|" + (es.getWarehouseId() != null ? es.getWarehouseId() : "") + "|" + (es.getBatchNo() != null ? es.getBatchNo() : "");
                if (alertedKeys.contains(key)) continue;

                Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, es.getDrugId()));
                String warehouseName = getWarehouseName(es.getWarehouseId());
                result.add(StockAlertVO.builder()
                        .drugId(es.getDrugId())
                        .drugName(drug != null ? drug.getDrugName() : null)
                        .warehouseId(es.getWarehouseId())
                        .warehouseName(warehouseName)
                        .batchNo(es.getBatchNo())
                        .alertType(1)
                        .alertTypeName(StockAlertVO.alertTypeName(1))
                        .currentStock(es.getCurrentStock())
                        .threshold(es.getCurrentStock())
                        .alertMessage((drug != null ? drug.getDrugName() : "") + "（仓库: " + (warehouseName != null ? warehouseName : es.getWarehouseId()) + "）已过期，有效期至: " + es.getExpiryDate())
                        .isHandled(false)
                        .createTime(es.getExpiryDate().atStartOfDay())
                        .build());
            }
        }
        return result;
    }

    // ==================== 预警处理 ====================

    @Override
    @Transactional
    public void handleAlert(Long alertId) {
        StockAlert alert = stockAlertMapper.selectById(alertId);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        alert.setIsHandled(1);
        alert.setHandleTime(LocalDateTime.now());
        stockAlertMapper.updateById(alert);
    }

    @Override
    @Transactional
    public void deleteAlert(Long alertId) {
        StockAlert alert = stockAlertMapper.selectById(alertId);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        stockAlertMapper.deleteById(alertId);

        // 低库存预警删除时，同步清除该批次库存为 0 的记录
        if (alert.getAlertType() == 0 && alert.getBatchNo() != null) {
            LambdaQueryWrapper<DrugStock> dw = new LambdaQueryWrapper<DrugStock>()
                    .eq(DrugStock::getDrugId, alert.getDrugId())
                    .eq(DrugStock::getCurrentStock, 0);
            if (alert.getWarehouseId() != null) {
                dw.eq(DrugStock::getWarehouseId, alert.getWarehouseId());
            } else {
                dw.isNull(DrugStock::getWarehouseId);
            }
            dw.eq(DrugStock::getBatchNo, alert.getBatchNo());
            drugStockMapper.delete(dw);
        }
    }

    // ==================== 库存调整 ====================

    @Override
    @Transactional
    public void adjustStock(String drugId, Integer quantity, String warehouseId, String batchNo,
                            LocalDate productionDate, LocalDate expiryDate, Integer minStock, Integer maxStock) {
        DrugStock stock;
        int newStock;

        if (quantity > 0 && batchNo != null && !batchNo.isBlank()) {
            // ===== 入库 + 有批号：按 (drug, warehouse, batch) 查找或新建 =====
            stock = drugStockMapper.selectOne(
                    new LambdaQueryWrapper<DrugStock>()
                            .eq(DrugStock::getDrugId, drugId)
                            .eq(DrugStock::getWarehouseId, warehouseId)
                            .eq(DrugStock::getBatchNo, batchNo));
            if (stock == null) {
                stock = new DrugStock();
                stock.setDrugId(drugId);
                stock.setWarehouseId(warehouseId);
                stock.setBatchNo(batchNo);
                stock.setCurrentStock(quantity);
                stock.setMinStock(minStock != null ? minStock : 10);
                stock.setMaxStock(maxStock != null ? maxStock : 1000);
                stock.setProductionDate(productionDate);
                stock.setExpiryDate(expiryDate);
                stock.setStatus(1);
                drugStockMapper.insert(stock);
                newStock = quantity;
            } else {
                newStock = stock.getCurrentStock() + quantity;
                stock.setCurrentStock(newStock);
                if (productionDate != null) stock.setProductionDate(productionDate);
                if (expiryDate != null) stock.setExpiryDate(expiryDate);
                if (minStock != null) stock.setMinStock(minStock);
                if (maxStock != null) stock.setMaxStock(maxStock);
                drugStockMapper.updateById(stock);
            }

            // ===== 逐批次检查预警（入库） =====
            String alertWhId = stock.getWarehouseId();
            autoResolveAlerts(drugId, alertWhId);

            if (stock.getMaxStock() != null && stock.getCurrentStock() > stock.getMaxStock()) {
                List<StockAlert> existing = getUnhandledAlertsByType(drugId, alertWhId, stock.getBatchNo(), 2);
                if (existing.isEmpty()) {
                    createOverStockAlert(drugId, alertWhId, stock.getBatchNo(), stock.getCurrentStock(), stock.getMaxStock());
                }
            }
        } else if (quantity > 0) {
            // ===== 入库 + 无批号：找到该仓库过期最晚的批次累加 =====
            LambdaQueryWrapper<DrugStock> wrapper = new LambdaQueryWrapper<DrugStock>()
                    .eq(DrugStock::getDrugId, drugId);
            if (warehouseId != null && !warehouseId.isBlank()) {
                wrapper.eq(DrugStock::getWarehouseId, warehouseId);
            }
            wrapper.orderByDesc(DrugStock::getExpiryDate);
            List<DrugStock> stocks = drugStockMapper.selectList(wrapper);
            if (stocks.isEmpty()) {
                throw new BusinessException("该药品暂无库存记录，入库请指定批号");
            }
            stock = stocks.get(0);
            newStock = stock.getCurrentStock() + quantity;
            stock.setCurrentStock(newStock);
            if (productionDate != null) stock.setProductionDate(productionDate);
            if (expiryDate != null) stock.setExpiryDate(expiryDate);
            if (minStock != null) stock.setMinStock(minStock);
            if (maxStock != null) stock.setMaxStock(maxStock);
            drugStockMapper.updateById(stock);

            // ===== 逐批次检查预警（无批号入库） =====
            String alertWhId = stock.getWarehouseId();
            autoResolveAlerts(drugId, alertWhId);

            if (stock.getMaxStock() != null && stock.getCurrentStock() > stock.getMaxStock()) {
                List<StockAlert> existing = getUnhandledAlertsByType(drugId, alertWhId, stock.getBatchNo(), 2);
                if (existing.isEmpty()) {
                    createOverStockAlert(drugId, alertWhId, stock.getBatchNo(), stock.getCurrentStock(), stock.getMaxStock());
                }
            }
        } else {
            // ===== 出库：按 expire 升序跨批次扣减（FEFO） =====
            LambdaQueryWrapper<DrugStock> wrapper = new LambdaQueryWrapper<DrugStock>().eq(DrugStock::getDrugId, drugId);
            if (warehouseId != null && !warehouseId.isBlank()) {
                wrapper.eq(DrugStock::getWarehouseId, warehouseId);
            }
            if (batchNo != null && !batchNo.isBlank()) {
                wrapper.eq(DrugStock::getBatchNo, batchNo);
            }
            wrapper.orderByAsc(DrugStock::getExpiryDate);
            List<DrugStock> stocks = drugStockMapper.selectList(wrapper);
            if (stocks.isEmpty()) {
                throw new BusinessException("该药品暂无库存记录");
            }

            int remaining = -quantity;
            List<DrugStock> affectedBatches = new ArrayList<>();
            for (DrugStock s : stocks) {
                if (remaining <= 0) break;
                int deduct = Math.min(s.getCurrentStock(), remaining);
                s.setCurrentStock(s.getCurrentStock() - deduct);
                remaining -= deduct;
                drugStockMapper.updateById(s);
                affectedBatches.add(s);
            }
            if (remaining > 0) {
                throw new BusinessException("库存不足，扣减后库存为负数");
            }

            stock = affectedBatches.get(0);
            newStock = stock.getCurrentStock();

            // ===== 逐批次检查预警 =====
            String alertWhId = stock.getWarehouseId();
            autoResolveAlerts(drugId, alertWhId);

            for (DrugStock batch : affectedBatches) {
                if (batch.getCurrentStock() <= batch.getMinStock()) {
                    List<StockAlert> existing = getUnhandledAlertsByType(drugId, alertWhId, batch.getBatchNo(), 0);
                    if (existing.isEmpty()) {
                        createLowStockAlert(drugId, alertWhId, batch.getBatchNo(), batch.getCurrentStock(), batch.getMinStock());
                    }
                }
            }
        }
    }

    // ==================== 过期销毁 ====================

    @Override
    @Transactional
    public void destroyExpired(String drugId, String warehouseId, String batchNo) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DrugStock> query = new LambdaQueryWrapper<DrugStock>()
                .eq(DrugStock::getDrugId, drugId)
                .lt(DrugStock::getExpiryDate, today);
        if (warehouseId != null && !warehouseId.isBlank()) {
            query.eq(DrugStock::getWarehouseId, warehouseId);
        }
        if (batchNo != null && !batchNo.isBlank()) {
            query.eq(DrugStock::getBatchNo, batchNo);
        }
        List<DrugStock> expired = drugStockMapper.selectList(query.orderByAsc(DrugStock::getExpiryDate));
        if (expired.isEmpty()) {
            throw new BusinessException("该药品暂无过期库存");
        }

        for (DrugStock stock : expired) {
            stock.setCurrentStock(0);
            drugStockMapper.updateById(stock);
        }

        // 标记该药品未处理的过期预警为已处理
        List<StockAlert> alerts = stockAlertMapper.selectList(
                new LambdaQueryWrapper<StockAlert>()
                        .eq(StockAlert::getDrugId, drugId)
                        .eq(StockAlert::getAlertType, 1)
                        .eq(StockAlert::getIsHandled, 0));
        for (StockAlert alert : alerts) {
            alert.setIsHandled(1);
            alert.setHandleTime(LocalDateTime.now());
            stockAlertMapper.updateById(alert);
        }

        // 销毁后逐批次触发低库存预警
        for (DrugStock s : expired) {
            List<StockAlert> existing = getUnhandledAlertsByType(drugId, s.getWarehouseId(), s.getBatchNo(), 0);
            if (existing.isEmpty()) {
                createLowStockAlert(drugId, s.getWarehouseId(), s.getBatchNo(), 0, s.getMinStock());
            }
        }
    }

    // ==================== 库存转移 ====================

    @Override
    @Transactional
    public void transferStock(String drugId, String fromWarehouseId, String toWarehouseId, Integer quantity, String batchNo) {
        // 1. 源仓库扣减
        LambdaQueryWrapper<DrugStock> sourceWrapper = new LambdaQueryWrapper<DrugStock>()
                .eq(DrugStock::getDrugId, drugId)
                .eq(DrugStock::getWarehouseId, fromWarehouseId);
        if (batchNo != null && !batchNo.isBlank()) {
            sourceWrapper.eq(DrugStock::getBatchNo, batchNo);
        }
        List<DrugStock> sources = drugStockMapper.selectList(sourceWrapper);
        if (sources.isEmpty()) {
            throw new BusinessException("源仓库无该药品库存记录");
        }
        DrugStock source = sources.get(0);
        if (source.getCurrentStock() < quantity) {
            throw new BusinessException("源仓库库存不足");
        }
        source.setCurrentStock(source.getCurrentStock() - quantity);
        drugStockMapper.updateById(source);

        // 2. 目标仓库增加（按 source 的 batch 查找或新建）
        List<DrugStock> targets = drugStockMapper.selectList(
                new LambdaQueryWrapper<DrugStock>()
                        .eq(DrugStock::getDrugId, drugId)
                        .eq(DrugStock::getWarehouseId, toWarehouseId)
                        .eq(DrugStock::getBatchNo, source.getBatchNo()));
        DrugStock target = targets.isEmpty() ? null : targets.get(0);
        if (target == null) {
            target = new DrugStock();
            target.setDrugId(drugId);
            target.setWarehouseId(toWarehouseId);
            target.setCurrentStock(quantity);
            target.setMinStock(source.getMinStock());
            target.setMaxStock(source.getMaxStock());
            target.setBatchNo(source.getBatchNo());
            target.setProductionDate(source.getProductionDate());
            target.setExpiryDate(source.getExpiryDate());
            target.setStatus(1);
            drugStockMapper.insert(target);
        } else {
            target.setCurrentStock(target.getCurrentStock() + quantity);
            drugStockMapper.updateById(target);
        }

        // 3. 源仓库扣减后检查是否触发低库存预警
        int sourceNewStock = source.getCurrentStock();
        if (sourceNewStock <= source.getMinStock()) {
            List<StockAlert> existingSource = getUnhandledAlertsByType(drugId, fromWarehouseId, source.getBatchNo(), 0);
            if (existingSource.isEmpty()) {
                createLowStockAlert(drugId, fromWarehouseId, source.getBatchNo(), sourceNewStock, source.getMinStock());
            }
        }

        // 4. 目标仓库增加后检查是否触发库存积压预警
        int targetNewStock = target.getCurrentStock();
        if (target.getMaxStock() != null && targetNewStock > target.getMaxStock()) {
            List<StockAlert> existingTarget = getUnhandledAlertsByType(drugId, toWarehouseId, target.getBatchNo(), 2);
            if (existingTarget.isEmpty()) {
                createOverStockAlert(drugId, toWarehouseId, target.getBatchNo(), targetNewStock, target.getMaxStock());
            }
        }
    }

    // ==================== 私有辅助方法 ====================

    /** 获取指定批次当前库存 */
    private int getBatchStock(String drugId, String warehouseId, String batchNo) {
        if (batchNo == null) return 0;
        DrugStock stock = drugStockMapper.selectOne(
                new LambdaQueryWrapper<DrugStock>()
                        .eq(DrugStock::getDrugId, drugId)
                        .eq(DrugStock::getWarehouseId, warehouseId)
                        .eq(DrugStock::getBatchNo, batchNo));
        return stock != null ? stock.getCurrentStock() : 0;
    }

    /** 按仓库汇总该药品的总库存 */
    private int getTotalStock(String drugId, String warehouseId) {
        LambdaQueryWrapper<DrugStock> wrapper = new LambdaQueryWrapper<DrugStock>()
                .eq(DrugStock::getDrugId, drugId);
        if (warehouseId != null) {
            wrapper.eq(DrugStock::getWarehouseId, warehouseId);
        } else {
            wrapper.isNull(DrugStock::getWarehouseId);
        }
        return drugStockMapper.selectList(wrapper)
                .stream().mapToInt(DrugStock::getCurrentStock).sum();
    }

    /** 按仓库汇总该药品的所有批次 max_stock 之和 */
    private int getTotalMaxStock(String drugId, String warehouseId) {
        LambdaQueryWrapper<DrugStock> wrapper = new LambdaQueryWrapper<DrugStock>()
                .eq(DrugStock::getDrugId, drugId);
        if (warehouseId != null) {
            wrapper.eq(DrugStock::getWarehouseId, warehouseId);
        } else {
            wrapper.isNull(DrugStock::getWarehouseId);
        }
        return drugStockMapper.selectList(wrapper)
                .stream().mapToInt(DrugStock::getMaxStock).sum();
    }

    /** 查询仓库名称 */
    private String getWarehouseName(String warehouseId) {
        if (warehouseId == null) return null;
        Warehouse wh = warehouseMapper.selectOne(
                new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getWarehouseId, warehouseId));
        return wh != null ? wh.getName() : null;
    }

    /** 获取指定批次的未处理预警列表 */
    private List<StockAlert> getUnhandledAlerts(String drugId, String warehouseId, String batchNo) {
        LambdaQueryWrapper<StockAlert> wrapper = new LambdaQueryWrapper<StockAlert>()
                .eq(StockAlert::getDrugId, drugId)
                .eq(StockAlert::getIsHandled, 0);
        if (warehouseId != null) {
            wrapper.eq(StockAlert::getWarehouseId, warehouseId);
        } else {
            wrapper.isNull(StockAlert::getWarehouseId);
        }
        if (batchNo != null) {
            wrapper.eq(StockAlert::getBatchNo, batchNo);
        }
        return stockAlertMapper.selectList(wrapper);
    }

    /** 获取指定类型 + 批次的未处理预警 */
    private List<StockAlert> getUnhandledAlertsByType(String drugId, String warehouseId, String batchNo, int alertType) {
        LambdaQueryWrapper<StockAlert> wrapper = new LambdaQueryWrapper<StockAlert>()
                .eq(StockAlert::getDrugId, drugId)
                .eq(StockAlert::getWarehouseId, warehouseId)
                .eq(StockAlert::getAlertType, alertType)
                .eq(StockAlert::getIsHandled, 0);
        if (batchNo != null) {
            wrapper.eq(StockAlert::getBatchNo, batchNo);
        } else {
            wrapper.isNull(StockAlert::getBatchNo);
        }
        return stockAlertMapper.selectList(wrapper);
    }

    /** 自动解除已恢复的预警（逐批次检查） */
    private void autoResolveAlerts(String drugId, String warehouseId) {
        List<StockAlert> existingAlerts = getUnhandledAlerts(drugId, warehouseId, null);
        for (StockAlert alert : existingAlerts) {
            int batchStock = getBatchStock(drugId, warehouseId, alert.getBatchNo());
            alert.setCurrentStock(batchStock);
            if (alert.getAlertType() == 0 && batchStock > alert.getThreshold()) {
                alert.setIsHandled(1);
                alert.setHandleTime(LocalDateTime.now());
            }
            if (alert.getAlertType() == 2 && batchStock <= alert.getThreshold()) {
                alert.setIsHandled(1);
                alert.setHandleTime(LocalDateTime.now());
            }
            stockAlertMapper.updateById(alert);
        }
    }

    /** 创建低库存预警 */
    private void createLowStockAlert(String drugId, String warehouseId, String batchNo, int totalStock, int minStock) {
        StockAlert alert = new StockAlert();
        alert.setDrugId(drugId);
        alert.setWarehouseId(warehouseId);
        alert.setBatchNo(batchNo);
        alert.setAlertType(0);
        alert.setCurrentStock(totalStock);
        alert.setThreshold(minStock);
        Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, drugId));
        StringBuilder loc = new StringBuilder();
        if (warehouseId != null || batchNo != null) {
            loc.append("（");
            if (warehouseId != null) loc.append("仓库: ").append(warehouseId);
            if (warehouseId != null && batchNo != null) loc.append("，");
            if (batchNo != null) loc.append("批号: ").append(batchNo);
            loc.append("）");
        }
        alert.setAlertMessage((drug != null ? drug.getDrugName() : "") + loc + "库存低于预警线（当前: " + totalStock + "，预警线: " + minStock + "）");
        alert.setIsHandled(0);
        stockAlertMapper.insert(alert);
    }

    /** 创建库存积压预警 */
    private void createOverStockAlert(String drugId, String warehouseId, String batchNo, int totalStock, int maxStock) {
        StockAlert alert = new StockAlert();
        alert.setDrugId(drugId);
        alert.setWarehouseId(warehouseId);
        alert.setBatchNo(batchNo);
        alert.setAlertType(2);
        alert.setCurrentStock(totalStock);
        alert.setThreshold(maxStock);
        Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, drugId));
        StringBuilder loc = new StringBuilder();
        if (warehouseId != null || batchNo != null) {
            loc.append("（");
            if (warehouseId != null) loc.append("仓库: ").append(warehouseId);
            if (warehouseId != null && batchNo != null) loc.append("，");
            if (batchNo != null) loc.append("批号: ").append(batchNo);
            loc.append("）");
        }
        alert.setAlertMessage((drug != null ? drug.getDrugName() : "") + loc + "库存积压（当前: " + totalStock + "，上限: " + maxStock + "）");
        alert.setIsHandled(0);
        stockAlertMapper.insert(alert);
    }

    @PostConstruct
    public void initAlertScan() {
        List<DrugStock> allStocks = drugStockMapper.selectList(
                new LambdaQueryWrapper<DrugStock>().gt(DrugStock::getCurrentStock, 0));
        for (DrugStock stock : allStocks) {
            String drugId = stock.getDrugId();
            String whId = stock.getWarehouseId();
            String batchNo = stock.getBatchNo();
            int currentStock = stock.getCurrentStock();

            if (currentStock <= stock.getMinStock()) {
                List<StockAlert> existing = stockAlertMapper.selectList(
                        new LambdaQueryWrapper<StockAlert>()
                                .eq(StockAlert::getDrugId, drugId)
                                .eq(StockAlert::getWarehouseId, whId)
                                .eq(StockAlert::getBatchNo, batchNo)
                                .eq(StockAlert::getAlertType, 0)
                                .eq(StockAlert::getIsHandled, 0));
                if (existing.isEmpty()) {
                    createLowStockAlert(drugId, whId, batchNo, currentStock, stock.getMinStock());
                }
            }
            if (stock.getMaxStock() != null && currentStock > stock.getMaxStock()) {
                List<StockAlert> existing = stockAlertMapper.selectList(
                        new LambdaQueryWrapper<StockAlert>()
                                .eq(StockAlert::getDrugId, drugId)
                                .eq(StockAlert::getWarehouseId, whId)
                                .eq(StockAlert::getBatchNo, batchNo)
                                .eq(StockAlert::getAlertType, 2)
                                .eq(StockAlert::getIsHandled, 0));
                if (existing.isEmpty()) {
                    createOverStockAlert(drugId, whId, batchNo, currentStock, stock.getMaxStock());
                }
            }
        }
    }
}

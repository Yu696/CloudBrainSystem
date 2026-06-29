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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final DrugStockMapper drugStockMapper;
    private final DrugMapper drugMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockAlertMapper stockAlertMapper;

    @Override
    public StockVO getStock(String drugId) {
        DrugStock stock = drugStockMapper.selectOne(
                new LambdaQueryWrapper<DrugStock>().eq(DrugStock::getDrugId, drugId));
        if (stock == null) {
            throw new BusinessException("该药品暂无库存记录");
        }

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
    public List<StockAlertVO> listAlerts(Integer type) {
        LambdaQueryWrapper<StockAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockAlert::getIsHandled, 0);
        if (type != null) {
            wrapper.eq(StockAlert::getAlertType, type);
        }
        wrapper.orderByDesc(StockAlert::getCreateTime);

        List<StockAlert> alerts = stockAlertMapper.selectList(wrapper);
        List<StockAlertVO> result = new ArrayList<>();
        for (StockAlert alert : alerts) {
            Drug drug = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, alert.getDrugId()));
            result.add(StockAlertVO.builder()
                    .id(alert.getId())
                    .drugId(alert.getDrugId())
                    .drugName(drug != null ? drug.getDrugName() : null)
                    .alertType(alert.getAlertType())
                    .alertTypeName(StockAlertVO.alertTypeName(alert.getAlertType()))
                    .currentStock(alert.getCurrentStock())
                    .threshold(alert.getThreshold())
                    .alertMessage(alert.getAlertMessage())
                    .isHandled(alert.getIsHandled() != null && alert.getIsHandled() == 1)
                    .handledBy(alert.getHandledBy())
                    .handleTime(alert.getHandleTime())
                    .createTime(alert.getCreateTime())
                    .build());
        }
        return result;
    }

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
    public PageResult<StockVO> listByWarehouse(String warehouseId, int page, int pageSize) {
        LambdaQueryWrapper<DrugStock> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null && !warehouseId.isBlank()) {
            wrapper.eq(DrugStock::getWarehouseId, warehouseId);
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
}

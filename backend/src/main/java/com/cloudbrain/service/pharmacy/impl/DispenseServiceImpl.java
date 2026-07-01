package com.cloudbrain.service.pharmacy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.pharmacy.DispenseRequest;
import com.cloudbrain.dto.response.pharmacy.DispenseResultVO;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.*;
import com.cloudbrain.service.pharmacy.DispenseService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispenseServiceImpl implements DispenseService {

    private final DrugMapper drugMapper;
    private final DrugStockMapper drugStockMapper;
    private final ShipRecordMapper shipRecordMapper;
    private final StockAlertMapper stockAlertMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final WalletTransactionMapper walletTxMapper;

    @Override
    @Transactional
    public DispenseResultVO dispense(DispenseRequest request) {
        // 校验处方存在且已审核
        Prescription prescription = prescriptionMapper.selectOne(
                new LambdaQueryWrapper<Prescription>().eq(Prescription::getPrescriptionId, request.getPrescriptionId()));
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (prescription.getStatus() != 2) {
            throw new BusinessException("处方未审核或状态不正确，无法发药");
        }

        // 校验是否已支付
        boolean paid = walletTxMapper.selectCount(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getRefId, request.getPrescriptionId())
                        .eq(WalletTransaction::getType, 2)) > 0;
        if (!paid) {
            throw new BusinessException("处方未支付，请先完成支付");
        }

        // 校验是否已发药（防止重复发药）
        boolean alreadyShipped = shipRecordMapper.selectCount(
                new LambdaQueryWrapper<ShipRecord>()
                        .eq(ShipRecord::getPrescriptionId, request.getPrescriptionId())) > 0;
        if (alreadyShipped) {
            throw new BusinessException("该处方已发药，不可重复操作");
        }

        int totalShipNum = 0;
        BigDecimal totalPayAmount = BigDecimal.ZERO;

        for (DispenseRequest.DispenseItem item : request.getItems()) {
            // 查询药品
            Drug drug = drugMapper.selectOne(
                    new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, item.getDrugId()));
            if (drug == null) {
                throw new BusinessException("药品不存在: " + item.getDrugId());
            }

            // 查询库存（多仓库下取第一个满足条件的记录）
            List<DrugStock> stocks = drugStockMapper.selectList(
                    new LambdaQueryWrapper<DrugStock>().eq(DrugStock::getDrugId, item.getDrugId()));
            if (stocks.isEmpty()) {
                throw new BusinessException("药品库存记录不存在: " + drug.getDrugName());
            }

            // 找第一个未过期且库存充足的仓库
            DrugStock stock = stocks.stream()
                    .filter(s -> s.getExpiryDate() == null || !s.getExpiryDate().isBefore(LocalDate.now()))
                    .filter(s -> s.getCurrentStock() >= item.getQuantity())
                    .findFirst()
                    .orElse(null);
            if (stock == null) {
                // 如果没有满足条件的，检查是否有未过期的但库存不足
                boolean hasStock = stocks.stream().anyMatch(s ->
                        s.getExpiryDate() == null || !s.getExpiryDate().isBefore(LocalDate.now()));
                if (!hasStock) {
                    throw new BusinessException("药品已过期，无法发药: " + drug.getDrugName());
                }
                throw new BusinessException("库存不足: " + drug.getDrugName()
                        + "，需要 " + item.getQuantity());
            }

            // 扣减库存（按 warehouse_id 精确更新）
            DrugStock updateEntity = new DrugStock();
            updateEntity.setCurrentStock(stock.getCurrentStock() - item.getQuantity());
            int affected = drugStockMapper.update(updateEntity,
                    new LambdaUpdateWrapper<DrugStock>()
                            .eq(DrugStock::getDrugId, item.getDrugId())
                            .eq(DrugStock::getWarehouseId, stock.getWarehouseId())
                            .ge(DrugStock::getCurrentStock, item.getQuantity()));
            if (affected == 0) {
                throw new BusinessException("药品库存扣减失败，请重试: " + drug.getDrugName());
            }

            totalShipNum += item.getQuantity();
            totalPayAmount = totalPayAmount.add(drug.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            // 发药后检查该仓库库存是否低于预警线
            int newStock = stock.getCurrentStock() - item.getQuantity();
            if (newStock <= stock.getMinStock()) {
                boolean alreadyExists = stockAlertMapper.selectCount(
                        new LambdaQueryWrapper<StockAlert>()
                                .eq(StockAlert::getDrugId, item.getDrugId())
                                .eq(StockAlert::getWarehouseId, stock.getWarehouseId())
                                .eq(StockAlert::getAlertType, 0)
                                .eq(StockAlert::getIsHandled, 0)) > 0;
                if (!alreadyExists) {
                    StockAlert alert = new StockAlert();
                    alert.setDrugId(item.getDrugId());
                    alert.setWarehouseId(stock.getWarehouseId());
                    alert.setAlertType(0);
                    alert.setCurrentStock(newStock);
                    alert.setThreshold(stock.getMinStock());
                    alert.setAlertMessage(drug.getDrugName() + "库存低于预警线（当前: "
                            + newStock + "，预警线: " + stock.getMinStock() + "）");
                    alert.setIsHandled(0);
                    stockAlertMapper.insert(alert);
                }
            }
        }

        // 生成发药记录
        String recordId = UUIDUtil.generateShipRecordId();
        ShipRecord record = new ShipRecord();
        record.setRecordId(recordId);
        record.setPrescriptionId(request.getPrescriptionId());
        record.setPatientId(request.getPatientId());
        record.setShipNum(totalShipNum);
        record.setPayAmount(totalPayAmount);
        record.setShipTime(LocalDateTime.now());
        record.setPrintStatus(0);
        shipRecordMapper.insert(record);

        // 更新处方状态为已发药
        prescription.setStatus(3);
        prescriptionMapper.updateById(prescription);

        return DispenseResultVO.builder()
                .recordId(recordId)
                .prescriptionId(request.getPrescriptionId())
                .shipNum(totalShipNum)
                .payAmount(totalPayAmount)
                .shipTime(record.getShipTime())
                .printStatus(0)
                .build();
    }
}

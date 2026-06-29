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

    @Override
    @Transactional
    public DispenseResultVO dispense(DispenseRequest request) {
        int totalShipNum = 0;
        BigDecimal totalPayAmount = BigDecimal.ZERO;

        for (DispenseRequest.DispenseItem item : request.getItems()) {
            // 查询药品
            Drug drug = drugMapper.selectOne(
                    new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, item.getDrugId()));
            if (drug == null) {
                throw new BusinessException("药品不存在: " + item.getDrugId());
            }

            // 查询库存
            DrugStock stock = drugStockMapper.selectOne(
                    new LambdaQueryWrapper<DrugStock>().eq(DrugStock::getDrugId, item.getDrugId()));
            if (stock == null) {
                throw new BusinessException("药品库存记录不存在: " + drug.getDrugName());
            }

            // 校验药品有效期
            if (stock.getExpiryDate() != null && stock.getExpiryDate().isBefore(LocalDate.now())) {
                throw new BusinessException("药品已过期，无法发药: " + drug.getDrugName());
            }

            // 校验库存充足（行级乐观锁）
            if (stock.getCurrentStock() < item.getQuantity()) {
                throw new BusinessException("库存不足: " + drug.getDrugName()
                        + "，需要 " + item.getQuantity() + "，当前库存 " + stock.getCurrentStock());
            }

            // 扣减库存（乐观锁：UPDATE drug_stock SET current_stock = current_stock - ? WHERE drug_id = ? AND current_stock >= ?）
            DrugStock updateEntity = new DrugStock();
            updateEntity.setCurrentStock(stock.getCurrentStock() - item.getQuantity());
            int affected = drugStockMapper.update(updateEntity,
                    new LambdaUpdateWrapper<DrugStock>()
                            .eq(DrugStock::getDrugId, item.getDrugId())
                            .ge(DrugStock::getCurrentStock, item.getQuantity()));
            if (affected == 0) {
                throw new BusinessException("药品库存扣减失败，请重试: " + drug.getDrugName());
            }

            totalShipNum += item.getQuantity();
            totalPayAmount = totalPayAmount.add(drug.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            // 发药后检查库存是否低于预警线
            int newStock = stock.getCurrentStock() - item.getQuantity();
            if (newStock <= stock.getMinStock()) {
                StockAlert alert = new StockAlert();
                alert.setDrugId(item.getDrugId());
                alert.setAlertType(0);
                alert.setCurrentStock(newStock);
                alert.setThreshold(stock.getMinStock());
                alert.setAlertMessage(drug.getDrugName() + "库存低于预警线（当前: "
                        + newStock + "，预警线: " + stock.getMinStock() + "）");
                alert.setIsHandled(0);
                stockAlertMapper.insert(alert);
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

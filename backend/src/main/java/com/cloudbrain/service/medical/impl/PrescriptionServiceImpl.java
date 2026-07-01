package com.cloudbrain.service.medical.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PrescriptionCreateRequest;
import com.cloudbrain.dto.response.PrescriptionItemVO;
import com.cloudbrain.dto.response.PrescriptionVO;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.*;
import com.cloudbrain.service.medical.PrescriptionService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final DrugMapper drugMapper;
    private final PatientMapper patientMapper;
    private final WalletTransactionMapper walletTxMapper;

    @Override
    @Transactional
    public PrescriptionVO createPrescription(PrescriptionCreateRequest request) {
        // 校验病历存在
        MedicalRecord record = medicalRecordMapper.selectOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRecordId, request.getRecordId()));
        if (record == null) {
            throw new BusinessException("病历不存在");
        }

        Prescription prescription = new Prescription();
        prescription.setPrescriptionId(UUIDUtil.generatePrescriptionId());
        prescription.setRecordId(request.getRecordId());
        prescription.setPatientId(record.getPatientId());
        prescription.setDoctorId(record.getDoctorId());
        prescription.setPrescriptionDesc(request.getPrescriptionDesc());
        prescription.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        prescription.setAuditStatus(0);
        prescription.setDiagnosisAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        // 创建处方明细
        if (request.getItems() != null) {
            for (PrescriptionCreateRequest.PrescriptionItemRequest itemReq : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setItemId(UUIDUtil.generatePrescriptionItemId());
                item.setPrescriptionId(prescription.getPrescriptionId());
                item.setDrugId(itemReq.getDrugId());
                item.setDrugName(itemReq.getDrugName());
                item.setSpec(itemReq.getSpec());
                item.setDosage(itemReq.getDosage());
                item.setFrequency(itemReq.getFrequency());
                item.setAdministration(itemReq.getAdministration());
                item.setDays(itemReq.getDays());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO);
                BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0));
                item.setSubtotal(subtotal);
                item.setRemark(itemReq.getRemark());
                prescriptionItemMapper.insert(item);
                totalAmount = totalAmount.add(subtotal);
            }
        }

        prescription.setTotalAmount(totalAmount);
        save(prescription);

        return PrescriptionVO.from(prescription);
    }

    @Override
    @Transactional
    public PrescriptionVO updatePrescription(String prescriptionId, PrescriptionCreateRequest request) {
        Prescription prescription = this.getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getPrescriptionId, prescriptionId));
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }

        prescription.setPrescriptionDesc(request.getPrescriptionDesc());
        prescription.setStatus(request.getStatus() != null ? request.getStatus() : prescription.getStatus());
        prescription.setDiagnosisAt(LocalDateTime.now());

        // 删除旧明细
        prescriptionItemMapper.delete(new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, prescriptionId));

        // 重新插入明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (PrescriptionCreateRequest.PrescriptionItemRequest itemReq : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setItemId(UUIDUtil.generatePrescriptionItemId());
                item.setPrescriptionId(prescriptionId);
                item.setDrugId(itemReq.getDrugId());
                item.setDrugName(itemReq.getDrugName());
                item.setSpec(itemReq.getSpec());
                item.setDosage(itemReq.getDosage());
                item.setFrequency(itemReq.getFrequency());
                item.setAdministration(itemReq.getAdministration());
                item.setDays(itemReq.getDays());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO);
                BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0));
                item.setSubtotal(subtotal);
                item.setRemark(itemReq.getRemark());
                prescriptionItemMapper.insert(item);
                totalAmount = totalAmount.add(subtotal);
            }
        }

        prescription.setTotalAmount(totalAmount);
        updateById(prescription);

        return PrescriptionVO.from(prescription);
    }

    @Override
    @Transactional
    public void deletePrescription(String prescriptionId) {
        Prescription prescription = this.getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getPrescriptionId, prescriptionId));
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        // 先删明细
        prescriptionItemMapper.delete(new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        // 再删处方
        removeById(prescription.getId());
    }

    @Override
    public List<PrescriptionVO> listPrescriptions(String recordId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getRecordId, recordId)
                .orderByDesc(Prescription::getCreateTime);

        return this.list(wrapper).stream()
                .map(PrescriptionVO::from)
                .toList();
    }

    @Override
    public PrescriptionVO getPrescriptionDetail(String prescriptionId) {
        Prescription prescription = this.getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getPrescriptionId, prescriptionId));
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }

        PrescriptionVO vo = PrescriptionVO.from(prescription);

        // 查询处方明细（补充 drug 表信息）
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        vo.setItems(items.stream().map(item -> {
            Drug drug = drugMapper.selectOne(
                    new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, item.getDrugId()));
            return PrescriptionItemVO.from(item, drug);
        }).toList());

        return vo;
    }

    @Override
    @Transactional
    public void auditPrescription(String prescriptionId) {
        Prescription prescription = this.getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getPrescriptionId, prescriptionId));
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (prescription.getStatus() != 1) {
            throw new BusinessException("仅待审核状态的处方可审核");
        }

        prescription.setStatus(2);
        prescription.setAuditStatus(1);

        // 记录审核人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            prescription.setAuditId(user.getUserId());
        }

        updateById(prescription);
    }

    @Override
    @Transactional
    public void payOrder(String prescriptionId) {
        Prescription prescription = this.getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getPrescriptionId, prescriptionId));
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (prescription.getStatus() != 2) {
            throw new BusinessException("仅已审核状态的处方可支付");
        }

        // 检查是否已支付
        boolean alreadyPaid = walletTxMapper.selectCount(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getRefId, prescriptionId)
                        .eq(WalletTransaction::getType, 2)) > 0;
        if (alreadyPaid) {
            throw new BusinessException("该处方已支付");
        }

        // 从钱包扣款
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, prescription.getPatientId()));
        if (patient == null) {
            throw new BusinessException("患者档案不存在");
        }

        BigDecimal fee = prescription.getTotalAmount() != null ? prescription.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal before = patient.getBalance() != null ? patient.getBalance() : BigDecimal.ZERO;
        if (before.compareTo(fee) < 0) {
            throw new BusinessException("钱包余额不足");
        }
        BigDecimal after = before.subtract(fee);
        patient.setBalance(after);
        patientMapper.updateById(patient);

        // 记录交易
        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionId(UUIDUtil.generateTransactionId());
        tx.setPatientId(prescription.getPatientId());
        tx.setType(2); // 药费
        tx.setAmount(fee.negate());
        tx.setBalanceAfter(after);
        tx.setRefId(prescriptionId);
        tx.setRemark("药费：处方" + prescriptionId);
        walletTxMapper.insert(tx);
    }
}

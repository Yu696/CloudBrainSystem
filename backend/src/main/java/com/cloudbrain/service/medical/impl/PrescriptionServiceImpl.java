package com.cloudbrain.service.medical.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PrescriptionCreateRequest;
import com.cloudbrain.dto.response.PrescriptionItemVO;
import com.cloudbrain.dto.response.PrescriptionVO;
import com.cloudbrain.entity.MedicalRecord;
import com.cloudbrain.entity.Prescription;
import com.cloudbrain.entity.PrescriptionItem;
import com.cloudbrain.mapper.MedicalRecordMapper;
import com.cloudbrain.mapper.PrescriptionItemMapper;
import com.cloudbrain.mapper.PrescriptionMapper;
import com.cloudbrain.service.medical.PrescriptionService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
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

        // 查询处方明细
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        vo.setItems(items.stream().map(PrescriptionItemVO::from).toList());

        return vo;
    }
}

package com.cloudbrain.service.pharmacy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.request.pharmacy.DrugAddRequest;
import com.cloudbrain.dto.request.pharmacy.DrugUpdateRequest;
import com.cloudbrain.dto.response.pharmacy.DrugVO;
import com.cloudbrain.entity.Drug;
import com.cloudbrain.mapper.DrugMapper;
import com.cloudbrain.service.pharmacy.DrugService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final DrugMapper drugMapper;

    @Override
    @Transactional
    public String add(DrugAddRequest request) {
        // 校验药品编码唯一性（如果填写了编码）
        if (StringUtils.hasText(request.getDrugCode())) {
            Long count = drugMapper.selectCount(
                    new LambdaQueryWrapper<Drug>().eq(Drug::getDrugCode, request.getDrugCode()));
            if (count > 0) {
                throw new BusinessException("药品编码已存在");
            }
        }

        Drug drug = new Drug();
        drug.setDrugId(UUIDUtil.generateDrugId());
        drug.setDrugCode(request.getDrugCode());
        drug.setDrugName(request.getDrugName());
        drug.setGenericName(request.getGenericName());
        drug.setIngredients(request.getIngredients());
        drug.setSpec(request.getSpec());
        drug.setDosageForm(request.getDosageForm());
        drug.setManufacturer(request.getManufacturer());
        drug.setUnit(request.getUnit());
        drug.setUnitPrice(request.getUnitPrice());
        drug.setPurchasePrice(request.getPurchasePrice());
        drug.setUsageMethod(request.getUsageMethod());
        drug.setCautiousCrowd(request.getCautiousCrowd());
        drug.setSideEffects(request.getSideEffects());
        drug.setDrugCategory(request.getDrugCategory());
        drug.setPrescriptionType(request.getPrescriptionType());
        drug.setStatus(1);
        drugMapper.insert(drug);

        return drug.getDrugId();
    }

    @Override
    @Transactional
    public void update(DrugUpdateRequest request) {
        Drug drug = drugMapper.selectOne(
                new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, request.getDrugId()));
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }

        // 校验编码唯一性
        if (StringUtils.hasText(request.getDrugCode())
                && !request.getDrugCode().equals(drug.getDrugCode())) {
            Long count = drugMapper.selectCount(
                    new LambdaQueryWrapper<Drug>().eq(Drug::getDrugCode, request.getDrugCode()));
            if (count > 0) {
                throw new BusinessException("药品编码已存在");
            }
        }

        if (request.getDrugCode() != null) drug.setDrugCode(request.getDrugCode());
        if (request.getDrugName() != null) drug.setDrugName(request.getDrugName());
        if (request.getGenericName() != null) drug.setGenericName(request.getGenericName());
        if (request.getIngredients() != null) drug.setIngredients(request.getIngredients());
        if (request.getSpec() != null) drug.setSpec(request.getSpec());
        if (request.getDosageForm() != null) drug.setDosageForm(request.getDosageForm());
        if (request.getManufacturer() != null) drug.setManufacturer(request.getManufacturer());
        if (request.getUnit() != null) drug.setUnit(request.getUnit());
        if (request.getUnitPrice() != null) drug.setUnitPrice(request.getUnitPrice());
        if (request.getPurchasePrice() != null) drug.setPurchasePrice(request.getPurchasePrice());
        if (request.getUsageMethod() != null) drug.setUsageMethod(request.getUsageMethod());
        if (request.getCautiousCrowd() != null) drug.setCautiousCrowd(request.getCautiousCrowd());
        if (request.getSideEffects() != null) drug.setSideEffects(request.getSideEffects());
        if (request.getDrugCategory() != null) drug.setDrugCategory(request.getDrugCategory());
        if (request.getPrescriptionType() != null) drug.setPrescriptionType(request.getPrescriptionType());
        drugMapper.updateById(drug);
    }

    @Override
    @Transactional
    public void delete(String drugId) {
        Drug drug = drugMapper.selectOne(
                new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, drugId));
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        drug.setStatus(0);
        drugMapper.updateById(drug);
    }

    @Override
    public PageResult<DrugVO> search(String keyword, String category, Integer prescriptionType, int page, int pageSize) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Drug::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Drug::getDrugName, keyword).or().like(Drug::getDrugCode, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Drug::getDrugCategory, category);
        }
        if (prescriptionType != null) {
            wrapper.eq(Drug::getPrescriptionType, prescriptionType);
        }
        wrapper.orderByDesc(Drug::getCreateTime);

        Page<Drug> result = drugMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<DrugVO> records = result.getRecords().stream().map(DrugVO::fromEntity).toList();
        return PageResult.of(result.getTotal(), page, pageSize, records);
    }

    @Override
    public DrugVO getDetail(String drugId) {
        Drug drug = drugMapper.selectOne(
                new LambdaQueryWrapper<Drug>().eq(Drug::getDrugId, drugId));
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        return DrugVO.fromEntity(drug);
    }

    @Override
    public List<DrugVO> all() {
        List<Drug> drugs = drugMapper.selectList(
                new LambdaQueryWrapper<Drug>().eq(Drug::getStatus, 1).orderByAsc(Drug::getDrugName));
        return drugs.stream().map(DrugVO::fromEntity).toList();
    }
}

package com.cloudbrain.service.ai.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.DiseaseKnowledge;
import com.cloudbrain.mapper.DiseaseKnowledgeMapper;
import com.cloudbrain.service.ai.DiseaseKbService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 疾病知识库服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiseaseKbServiceImpl
        extends ServiceImpl<DiseaseKnowledgeMapper, DiseaseKnowledge>
        implements DiseaseKbService {

    @Override
    public List<DiseaseKnowledge> search(String keyword) {
        LambdaQueryWrapper<DiseaseKnowledge> wrapper = new LambdaQueryWrapper<DiseaseKnowledge>()
                .like(keyword != null && !keyword.isEmpty(),
                        DiseaseKnowledge::getDiseaseName, keyword)
                .orderByDesc(DiseaseKnowledge::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<DiseaseKnowledge> listAll() {
        return this.list(new LambdaQueryWrapper<DiseaseKnowledge>()
                .eq(DiseaseKnowledge::getStatus, 1)
                .orderByDesc(DiseaseKnowledge::getCreateTime));
    }

    @Override
    public DiseaseKnowledge getByDiseaseId(String diseaseId) {
        DiseaseKnowledge disease = this.getOne(new LambdaQueryWrapper<DiseaseKnowledge>()
                .eq(DiseaseKnowledge::getDiseaseId, diseaseId));
        if (disease == null) {
            throw new BusinessException("疾病条目不存在");
        }
        return disease;
    }

    @Override
    @Transactional
    public DiseaseKnowledge create(DiseaseKnowledge disease) {
        if (disease.getDiseaseName() == null || disease.getDiseaseName().isBlank()) {
            throw new BusinessException("疾病名称不能为空");
        }

        // 检查疾病名称唯一性
        if (lambdaQuery().eq(DiseaseKnowledge::getDiseaseName, disease.getDiseaseName()).count() > 0) {
            throw new BusinessException("该疾病名称已存在");
        }

        disease.setDiseaseId(UUIDUtil.generateDiseaseId());
        disease.setStatus(disease.getStatus() != null ? disease.getStatus() : 1);

        save(disease);
        return disease;
    }

    @Override
    @Transactional
    public DiseaseKnowledge update(DiseaseKnowledge disease) {
        if (disease.getDiseaseId() == null) {
            throw new BusinessException("疾病ID不能为空");
        }

        DiseaseKnowledge existing = getByDiseaseId(disease.getDiseaseId());

        // 校验疾病名称唯一性
        if (disease.getDiseaseName() != null
                && !disease.getDiseaseName().equals(existing.getDiseaseName())) {
            if (lambdaQuery().eq(DiseaseKnowledge::getDiseaseName, disease.getDiseaseName()).count() > 0) {
                throw new BusinessException("该疾病名称已存在");
            }
            existing.setDiseaseName(disease.getDiseaseName());
        }

        if (disease.getIcdCode() != null) existing.setIcdCode(disease.getIcdCode());
        if (disease.getCategory() != null) existing.setCategory(disease.getCategory());
        if (disease.getRelatedDepartmentId() != null) existing.setRelatedDepartmentId(disease.getRelatedDepartmentId());
        if (disease.getSymptoms() != null) existing.setSymptoms(disease.getSymptoms());
        if (disease.getRiskFactors() != null) existing.setRiskFactors(disease.getRiskFactors());
        if (disease.getDiagnosisBasis() != null) existing.setDiagnosisBasis(disease.getDiagnosisBasis());
        if (disease.getTreatmentPlan() != null) existing.setTreatmentPlan(disease.getTreatmentPlan());
        if (disease.getSimilarityDiseases() != null) existing.setSimilarityDiseases(disease.getSimilarityDiseases());
        if (disease.getStatus() != null) existing.setStatus(disease.getStatus());

        updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(String diseaseId) {
        DiseaseKnowledge existing = getByDiseaseId(diseaseId);
        removeById(existing.getId());
    }
}

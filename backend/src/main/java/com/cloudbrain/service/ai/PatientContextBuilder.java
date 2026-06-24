package com.cloudbrain.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.ai.PatientContext;
import com.cloudbrain.dto.ai.PatientContext.CurrentMedication;
import com.cloudbrain.dto.ai.PatientContext.RecentRecord;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 患者背景信息构建器
 * 根据 patientId 自动查询并填充 PatientContext，前端无需传递患者病史
 *
 * 性能：4 次 SQL 完成所有数据组装（避免 N+1）
 *   1. Patient 档案
 *   2. Prescription（最近90天）
 *   3. PrescriptionItem（IN 批量查询）
 *   4. Drug（IN 批量查询）
 *
 * @author 刘鹏杰
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientContextBuilder {

    private final PatientMapper patientMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final DrugMapper drugMapper;
    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * 根据 patientId 构建完整的患者背景信息
     *
     * @param patientId 患者ID
     * @return 完整的患者背景上下文
     * @throws BusinessException 患者不存在时抛出
     */
    public PatientContext build(String patientId) {
        // 1. 查患者档案
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>()
                        .eq(Patient::getPatientId, patientId));
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 2. 计算年龄
        int age = 0;
        if (patient.getBirthDate() != null) {
            age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
        }

        // 3. 查当前用药 —— 批量查询，避免 N+1
        List<CurrentMedication> medications = buildCurrentMedications(patientId);

        // 4. 查最近就诊记录
        List<RecentRecord> records = buildRecentRecords(patientId);

        return PatientContext.builder()
                .age(age)
                .gender(patient.getGender() != null ? patient.getGender() : 0)
                .allergyHistory(patient.getAllergyHistory())
                .medicalHistory(patient.getMedicalHistory())
                .geneticDiseases(patient.getGeneticDiseases())
                .bloodType(patient.getBloodType())
                .currentMedications(medications)
                .recentRecords(records)
                .build();
    }

    /**
     * 查询患者当前用药（最近90天处方）
     * 批量查询优化：Prescription → PrescriptionItem(IN) → Drug(IN)
     */
    private List<CurrentMedication> buildCurrentMedications(String patientId) {
        // Step A: 查最近90天的处方
        List<Prescription> prescriptions = prescriptionMapper.selectList(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getPatientId, patientId)
                        .ge(Prescription::getCreateTime,
                                java.time.LocalDateTime.now().minusDays(90))
                        .orderByDesc(Prescription::getCreateTime));

        if (prescriptions.isEmpty()) {
            return List.of();
        }

        // Step B: 批量查所有处方的药品明细
        List<String> prescriptionIds = prescriptions.stream()
                .map(Prescription::getPrescriptionId)
                .toList();
        List<PrescriptionItem> allItems = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .in(PrescriptionItem::getPrescriptionId, prescriptionIds));

        if (allItems.isEmpty()) {
            return List.of();
        }

        // Step C: 批量查药品名称
        List<String> drugIds = allItems.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .toList();
        Map<String, Drug> drugMap = drugMapper.selectList(
                        new LambdaQueryWrapper<Drug>()
                                .in(Drug::getDrugId, drugIds))
                .stream()
                .collect(Collectors.toMap(Drug::getDrugId, Function.identity()));

        // Step D: 组装结果
        return allItems.stream()
                .map(item -> {
                    Drug drug = drugMap.get(item.getDrugId());
                    return CurrentMedication.builder()
                            .drugName(drug != null ? drug.getDrugName() : (item.getDrugName() != null ? item.getDrugName() : "未知"))
                            .dosage(item.getDosage())
                            .frequency(item.getFrequency())
                            .build();
                })
                .distinct()
                .limit(10)
                .toList();
    }

    /**
     * 查询患者近期就诊记录（最近5条）
     */
    private List<RecentRecord> buildRecentRecords(String patientId) {
        List<MedicalRecord> records = medicalRecordMapper.selectList(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .orderByDesc(MedicalRecord::getCreateTime)
                        .last("LIMIT 5"));

        if (records.isEmpty()) {
            return List.of();
        }

        return records.stream()
                .map(r -> RecentRecord.builder()
                        .date(r.getCreateTime() != null
                                ? r.getCreateTime().toLocalDate().toString()
                                : null)
                        .diagnosis(r.getDiagnosis())
                        .build())
                .toList();
    }
}

package com.cloudbrain.service.medical.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.ExaminationOrderCreateRequest;
import com.cloudbrain.dto.request.ExaminationResultCreateRequest;
import com.cloudbrain.dto.response.ExaminationOrderVO;
import com.cloudbrain.dto.response.ExaminationResultVO;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.*;
import com.cloudbrain.entity.ExaminationOrder;
import com.cloudbrain.entity.ExaminationResult;
import com.cloudbrain.entity.MedicalRecord;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.WalletTransaction;
import com.cloudbrain.mapper.ExaminationOrderMapper;
import com.cloudbrain.mapper.ExaminationResultMapper;
import com.cloudbrain.mapper.MedicalRecordMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.WalletTransactionMapper;
import com.cloudbrain.service.medical.ExaminationService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExaminationServiceImpl extends ServiceImpl<ExaminationOrderMapper, ExaminationOrder> implements ExaminationService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final ExaminationResultMapper examinationResultMapper;
    private final PatientMapper patientMapper;
    private final WalletTransactionMapper walletTxMapper;
    private final MedicalImageMapper medicalImageMapper;
    private final DoctorMapper doctorMapper;

    /** 检查项目价格表 */
    private static final Map<String, BigDecimal> EXAM_PRICE_MAP = Map.<String, BigDecimal>ofEntries(
        // 实验室检查
        Map.entry("血常规", new BigDecimal("35.00")),
        Map.entry("尿常规", new BigDecimal("20.00")),
        Map.entry("肝功能全套", new BigDecimal("120.00")),
        Map.entry("肾功能全套", new BigDecimal("110.00")),
        Map.entry("血糖", new BigDecimal("25.00")),
        Map.entry("血脂全套", new BigDecimal("130.00")),
        Map.entry("电解质", new BigDecimal("45.00")),
        Map.entry("凝血功能", new BigDecimal("80.00")),
        Map.entry("心肌酶谱", new BigDecimal("90.00")),
        Map.entry("甲状腺功能", new BigDecimal("150.00")),
        Map.entry("肿瘤标志物", new BigDecimal("280.00")),
        // 影像学检查
        Map.entry("X光胸片", new BigDecimal("150.00")),
        Map.entry("CT平扫", new BigDecimal("450.00")),
        Map.entry("CT增强", new BigDecimal("800.00")),
        Map.entry("MRI平扫", new BigDecimal("900.00")),
        Map.entry("MRI增强", new BigDecimal("1500.00")),
        Map.entry("B超腹部", new BigDecimal("180.00")),
        Map.entry("B超心脏", new BigDecimal("220.00")),
        Map.entry("B超甲状腺", new BigDecimal("150.00")),
        Map.entry("B超妇科", new BigDecimal("180.00")),
        Map.entry("PET-CT", new BigDecimal("7000.00")),
        // 功能检查
        Map.entry("心电图", new BigDecimal("50.00")),
        Map.entry("动态心电图", new BigDecimal("280.00")),
        Map.entry("肺功能检测", new BigDecimal("160.00")),
        Map.entry("脑电图", new BigDecimal("200.00")),
        Map.entry("肌电图", new BigDecimal("250.00")),
        Map.entry("胃镜", new BigDecimal("350.00")),
        Map.entry("肠镜", new BigDecimal("400.00")),
        Map.entry("支气管镜", new BigDecimal("500.00")),
        Map.entry("骨密度检测", new BigDecimal("180.00")),
        Map.entry("视力检查", new BigDecimal("30.00"))
    );

    @Override
    @Transactional
    public ExaminationOrderVO createOrder(ExaminationOrderCreateRequest request) {
        MedicalRecord record = medicalRecordMapper.selectOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRecordId, request.getRecordId()));
        if (record == null) {
            throw new BusinessException("病历不存在");
        }

        ExaminationOrder order = new ExaminationOrder();
        order.setOrderId(UUIDUtil.generateExaminationOrderId());
        order.setRecordId(request.getRecordId());
        order.setPatientId(record.getPatientId());
        order.setDoctorId(record.getDoctorId());
        order.setExamName(request.getExamName());
        order.setExamCategory(request.getExamCategory());
        order.setExamPurpose(request.getExamPurpose());
        order.setStatus(0);
        order.setIsAiRecommended(0);
        // 费用：传了用传入值，否则按价目表，默认0
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            amount = EXAM_PRICE_MAP.getOrDefault(request.getExamName(), BigDecimal.ZERO);
        }
        order.setAmount(amount);
        save(order);

        return ExaminationOrderVO.from(order);
    }

    @Override
    @Transactional
    public ExaminationOrderVO updateOrder(String orderId, ExaminationOrderCreateRequest request) {
        ExaminationOrder order = this.getOne(new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getOrderId, orderId));
        if (order == null) {
            throw new BusinessException("检查单不存在");
        }
        order.setExamName(request.getExamName());
        order.setExamCategory(request.getExamCategory());
        order.setExamPurpose(request.getExamPurpose());
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            amount = EXAM_PRICE_MAP.getOrDefault(request.getExamName(), BigDecimal.ZERO);
        }
        order.setAmount(amount);
        updateById(order);
        return ExaminationOrderVO.from(order);
    }

    @Override
    @Transactional
    public void deleteOrder(String orderId) {
        ExaminationOrder order = this.getOne(new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getOrderId, orderId));
        if (order == null) {
            throw new BusinessException("检查单不存在");
        }
        // 删除关联的检查结果
        examinationResultMapper.delete(new LambdaQueryWrapper<ExaminationResult>()
                .eq(ExaminationResult::getOrderId, orderId));
        removeById(order.getId());
    }

    @Override
    public List<ExaminationOrderVO> listOrders(String recordId) {
        LambdaQueryWrapper<ExaminationOrder> wrapper = new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getRecordId, recordId)
                .orderByDesc(ExaminationOrder::getCreateTime);

        return this.list(wrapper).stream()
                .map(ExaminationOrderVO::from)
                .toList();
    }

    @Override
    public List<ExaminationOrderVO> listImagingOrders(String doctorId) {
        LambdaQueryWrapper<ExaminationOrder> wrapper = new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getExamCategory, 1) // 影像学检查
                .in(ExaminationOrder::getStatus, 0, 1, 2, 3) // 已开单、已缴费、检查中、已完成
                .orderByDesc(ExaminationOrder::getCreateTime);
        if (doctorId != null && !doctorId.isBlank()) {
            wrapper.eq(ExaminationOrder::getDoctorId, doctorId);
        }
        List<ExaminationOrder> orders = this.list(wrapper);
        List<ExaminationOrderVO> voList = orders.stream()
                .map(ExaminationOrderVO::from)
                .collect(Collectors.toList());

        // 批量查询患者姓名
        List<String> patientIds = orders.stream()
                .map(ExaminationOrder::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!patientIds.isEmpty()) {
            Map<String, String> nameMap = patientMapper.selectList(
                    new LambdaQueryWrapper<Patient>()
                            .in(Patient::getPatientId, patientIds))
                    .stream()
                    .collect(Collectors.toMap(Patient::getPatientId, Patient::getName, (a, b) -> a));
            for (ExaminationOrderVO vo : voList) {
                vo.setPatientName(nameMap.get(vo.getPatientId()));
            }
        }

        return voList;
    }

    @Override
    public ExaminationOrderVO getDetail(String orderId) {
        ExaminationOrder order = this.getOne(new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getOrderId, orderId));
        if (order == null) {
            throw new BusinessException("检查单不存在");
        }
        return ExaminationOrderVO.from(order);
    }

    @Override
    public ExaminationResultVO getResult(String orderId) {
        ExaminationResult result = examinationResultMapper.selectOne(
                new LambdaQueryWrapper<ExaminationResult>()
                        .eq(ExaminationResult::getOrderId, orderId));
        if (result == null) {
            return null;
        }
        return ExaminationResultVO.from(result);
    }

    @Override
    public ExaminationResultVO saveResult(ExaminationResultCreateRequest request) {
        // 解析 orderId：优先用传入值，否则通过 imageId 查找或自动创建
        String orderId = resolveOrderId(request);

        // 验证检查单存在
        ExaminationOrder order = this.getOne(new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getOrderId, orderId));
        if (order == null) {
            throw new BusinessException("检查单不存在");
        }

        // 查找已有结果，有则更新，无则新增
        ExaminationResult result = examinationResultMapper.selectOne(
                new LambdaQueryWrapper<ExaminationResult>()
                        .eq(ExaminationResult::getOrderId, orderId));

        boolean isNew = (result == null);
        if (isNew) {
            result = new ExaminationResult();
            result.setOrderId(orderId);
            result.setResultId(UUIDUtil.generateExaminationResultId());
        }

        result.setResultData(request.getResultData());
        result.setReferenceRange(request.getReferenceRange());
        result.setIsAbnormal(request.getIsAbnormal() != null ? request.getIsAbnormal() : 0);
        result.setDoctorOpinion(request.getDoctorOpinion());
        result.setReportFileUrl(request.getReportFileUrl());
        result.setAiAnalysis(request.getAiAnalysis());
        result.setResultTime(LocalDateTime.now());

        if (isNew) {
            examinationResultMapper.insert(result);
        } else {
            examinationResultMapper.updateById(result);
        }

        // 更新检查单状态为已完成(3)
        if (order.getStatus() != null && order.getStatus() < 3) {
            order.setStatus(3);
            updateById(order);
        }

        return ExaminationResultVO.from(result);
    }

    @Override
    @Transactional
    public void payOrder(String orderId) {
        ExaminationOrder order = this.getOne(new LambdaQueryWrapper<ExaminationOrder>()
                .eq(ExaminationOrder::getOrderId, orderId));
        if (order == null) {
            throw new BusinessException("检查单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("该检查单已支付或无需支付");
        }

        // 从钱包扣款
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, order.getPatientId()));
        if (patient == null) {
            throw new BusinessException("患者档案不存在");
        }

        BigDecimal fee = order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
        BigDecimal before = patient.getBalance() != null ? patient.getBalance() : BigDecimal.ZERO;
        BigDecimal after = before.subtract(fee);
        patient.setBalance(after);
        patientMapper.updateById(patient);

        // 记录交易
        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionId(UUIDUtil.generateTransactionId());
        tx.setPatientId(order.getPatientId());
        tx.setType(3); // 检查费
        tx.setAmount(fee.negate());
        tx.setBalanceAfter(after);
        tx.setRefId(order.getOrderId());
        tx.setRemark("检查费：" + order.getExamName());
        walletTxMapper.insert(tx);

        // 更新检查单状态
        order.setStatus(1); // 已缴费
        updateById(order);
    }

    /**
     * 解析检查单 ID：优先用传入的 orderId，否则通过 imageId 查找或自动创建检查单
     */
    private String resolveOrderId(ExaminationResultCreateRequest request) {
        String orderId = request.getOrderId();
        if (orderId != null && !orderId.isBlank()) {
            return orderId;
        }

        String imageId = request.getImageId();
        if (imageId == null || imageId.isBlank()) {
            throw new BusinessException("检查单ID和影像ID不能同时为空");
        }

        MedicalImage image = medicalImageMapper.selectOne(
                new LambdaQueryWrapper<MedicalImage>().eq(MedicalImage::getImageId, imageId));
        if (image == null) {
            throw new BusinessException("影像不存在");
        }

        // 影像已关联检查单，直接使用
        String existingOrderId = image.getExaminationId();
        if (existingOrderId != null && !existingOrderId.isBlank()) {
            ExaminationOrder existing = this.getOne(new LambdaQueryWrapper<ExaminationOrder>()
                    .eq(ExaminationOrder::getOrderId, existingOrderId));
            if (existing != null) {
                return existingOrderId;
            }
        }

        // 自动创建检查单
        String newOrderId = (existingOrderId != null && !existingOrderId.isBlank())
                ? existingOrderId
                : UUIDUtil.generateExaminationOrderId();

        ExaminationOrder order = new ExaminationOrder();
        order.setOrderId(newOrderId);
        order.setPatientId(image.getPatientId());
        order.setDoctorId(getCurrentDoctorId());
        order.setExamCategory(1); // 影像学检查
        order.setExamName(image.getModality() != null ? image.getModality() : "影像检查");
        order.setExamPurpose(image.getBodyPart());
        order.setStatus(0);
        order.setAmount(BigDecimal.ZERO);
        save(order);

        // 更新影像的 examinationId
        image.setExaminationId(newOrderId);
        medicalImageMapper.updateById(image);

        return newOrderId;
    }

    /** 获取当前登录医生的 doctorId */
    private String getCurrentDoctorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            Doctor doctor = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, user.getUserId()));
            if (doctor != null) {
                return doctor.getDoctorId();
            }
        }
        throw new BusinessException("无法获取医生信息");
    }
}

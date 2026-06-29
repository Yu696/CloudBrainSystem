package com.cloudbrain.controller.wallet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.ExaminationOrder;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.WalletTransaction;
import com.cloudbrain.mapper.AppointmentMapper;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.ExaminationOrderMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.WalletTransactionMapper;
import com.cloudbrain.util.UUIDUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "钱包管理")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController extends BaseController {

    private final PatientMapper patientMapper;
    private final WalletTransactionMapper walletTxMapper;
    private final AppointmentMapper appointmentMapper;
    private final ExaminationOrderMapper examinationOrderMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;

    @Data
    public static class RechargeRequest {
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;
        @NotBlank(message = "患者ID不能为空")
        private String patientId;
    }

    @Operation(summary = "获取钱包余额")
    @GetMapping("/balance")
    public Result<Map<String, Object>> balance(
            @Parameter(description = "患者 ID") @RequestParam String patientId) {
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, patientId));
        if (patient == null) {
            return fail("患者不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientId", patientId);
        result.put("balance", patient.getBalance());
        return success(result);
    }

    @Operation(summary = "充值")
    @PostMapping("/recharge")
    @Transactional
    public Result<Map<String, Object>> recharge(@RequestBody RechargeRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return fail("充值金额必须大于0");
        }

        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, request.getPatientId()));
        if (patient == null) {
            return fail("患者不存在");
        }

        BigDecimal before = patient.getBalance();
        BigDecimal after = before.add(request.getAmount());
        patient.setBalance(after);
        patientMapper.updateById(patient);

        // 记录交易
        WalletTransaction tx = new WalletTransaction();
        tx.setTransactionId(UUIDUtil.generateTransactionId());
        tx.setPatientId(request.getPatientId());
        tx.setType(0); // 充值
        tx.setAmount(request.getAmount());
        tx.setBalanceAfter(after);
        tx.setRemark("充值");
        walletTxMapper.insert(tx);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionId", tx.getTransactionId());
        result.put("balanceBefore", before);
        result.put("balanceAfter", after);
        return success(result);
    }

    @Operation(summary = "交易记录列表")
    @GetMapping("/transactions")
    public Result<List<Map<String, Object>>> transactions(
            @Parameter(description = "患者 ID") @RequestParam String patientId) {
        List<WalletTransaction> txs = walletTxMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getPatientId, patientId)
                        .orderByDesc(WalletTransaction::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (WalletTransaction tx : txs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("transactionId", tx.getTransactionId());
            item.put("type", tx.getType());
            item.put("typeName", switch (tx.getType()) {
                case 0 -> "充值";
                case 1 -> "挂号费";
                case 2 -> "药费";
                case 3 -> "检查费";
                default -> "其他";
            });
            item.put("amount", tx.getAmount());
            item.put("balanceAfter", tx.getBalanceAfter());
            item.put("refId", tx.getRefId());
            item.put("remark", tx.getRemark());
            item.put("createTime", tx.getCreateTime());
            result.add(item);
        }
        return success(result);
    }

    @Operation(summary = "待支付订单列表")
    @GetMapping("/pending-orders")
    public Result<List<Map<String, Object>>> pendingOrders(
            @Parameter(description = "患者 ID") @RequestParam String patientId) {
        List<Map<String, Object>> orders = new ArrayList<>();

        // 1. 待支付挂号（appointment status=0）
        List<Appointment> appts = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getPatientId, patientId)
                        .eq(Appointment::getPaymentStatus, 0)
                        .orderByDesc(Appointment::getCreateTime));
        for (Appointment a : appts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "挂号费");
            item.put("refId", a.getAppointmentId());
            item.put("amount", a.getTotalFee());
            item.put("desc", a.getTimeSlotDesc() + " " + (a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : ""));
            // 医生名
            Doctor doc = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, a.getDoctorId()));
            if (doc != null) {
                User docUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, doc.getUserId()));
                item.put("doctorName", docUser != null ? docUser.getRealName() : doc.getDoctorId());
            }
            item.put("createTime", a.getCreateTime());
            item.put("routePath", "/appointment/pay/" + a.getAppointmentId());
            orders.add(item);
        }

        // 2. 待支付检查单（examination_order status=0）
        List<ExaminationOrder> exams = examinationOrderMapper.selectList(
                new LambdaQueryWrapper<ExaminationOrder>()
                        .eq(ExaminationOrder::getPatientId, patientId)
                        .eq(ExaminationOrder::getStatus, 0)
                        .orderByDesc(ExaminationOrder::getCreateTime));
        for (ExaminationOrder e : exams) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "检查费");
            item.put("refId", e.getOrderId());
            item.put("amount", e.getAmount());
            item.put("desc", e.getExamName());
            item.put("createTime", e.getCreateTime());
            item.put("routePath", "/exam/pay/" + e.getOrderId());
            orders.add(item);
        }

        return success(orders);
    }
}

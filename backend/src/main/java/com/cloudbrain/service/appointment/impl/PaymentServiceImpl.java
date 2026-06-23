package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PaymentCreateRequest;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Payment;
import com.cloudbrain.entity.TimeSlot;
import com.cloudbrain.mapper.AppointmentMapper;
import com.cloudbrain.mapper.PaymentMapper;
import com.cloudbrain.mapper.TimeSlotMapper;
import com.cloudbrain.service.appointment.PaymentService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final AppointmentMapper appointmentMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    @Transactional
    public Payment create(PaymentCreateRequest request) {
        Appointment appointment = appointmentMapper.selectOne(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getAppointmentId, request.getAppointmentId()));
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }

        if (appointment.getPaymentStatus() != 0) {
            throw new BusinessException("该预约已支付或无需支付");
        }

        // 模拟支付成功
        Payment payment = new Payment();
        payment.setPaymentId(UUIDUtil.generatePaymentId());
        payment.setAppointmentId(request.getAppointmentId());
        payment.setPatientId(request.getPatientId());
        payment.setAmount(appointment.getTotalFee());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(1);  // 0=待支付 1=已支付 2=已退款
        payment.setTradeNo("TRD_" + System.currentTimeMillis());
        payment.setPaymentTime(LocalDateTime.now());

        this.save(payment);

        // 更新预约状态
        appointment.setPaymentStatus(1);
        appointment.setStatus(1);
        appointmentMapper.updateById(appointment);

        // 更新时段状态
        if (appointment.getTimeSlotId() != null) {
            TimeSlot slot = timeSlotMapper.selectOne(
                    new LambdaQueryWrapper<TimeSlot>().eq(TimeSlot::getSlotId, appointment.getTimeSlotId()));
            if (slot != null) {
                slot.setStatus(2);
                slot.setAppointmentId(appointment.getAppointmentId());
                slot.setLockToken(null);
                slot.setLockedBy(null);
                slot.setLockExpiryTime(null);
                timeSlotMapper.updateById(slot);
            }
        }

        return payment;
    }

    @Override
    public Payment getStatus(String paymentId) {
        Payment payment = this.getOne(new LambdaQueryWrapper<Payment>().eq(Payment::getPaymentId, paymentId));
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        return payment;
    }
}

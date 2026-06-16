package com.cloudbrain.service.appointment;

import com.cloudbrain.dto.request.PaymentCreateRequest;
import com.cloudbrain.entity.Payment;

public interface PaymentService {
    Payment create(PaymentCreateRequest request);
    Payment getStatus(String paymentId);
}

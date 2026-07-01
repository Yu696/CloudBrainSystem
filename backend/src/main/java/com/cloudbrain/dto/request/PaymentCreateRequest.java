package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCreateRequest {

    @NotBlank(message = "预约ID不能为空")
    private String appointmentId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;  // 0=医保卡 1=现金 2=扫码 3=银行卡 4=钱包支付
}

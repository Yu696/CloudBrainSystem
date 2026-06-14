package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.PaymentCreateRequest;
import com.cloudbrain.entity.Payment;
import com.cloudbrain.service.appointment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "支付管理")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController extends BaseController {

    private final PaymentService paymentService;

    @Operation(summary = "创建支付")
    @PostMapping("/create")
    public Result<Payment> create(@Valid @RequestBody PaymentCreateRequest request) {
        return success(paymentService.create(request));
    }

    @Operation(summary = "支付状态")
    @GetMapping("/status")
    public Result<Payment> status(@RequestParam String paymentId) {
        return success(paymentService.getStatus(paymentId));
    }
}

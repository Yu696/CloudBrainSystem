package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.AppointmentBookRequest;
import com.cloudbrain.dto.request.AppointmentCancelRequest;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.service.appointment.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "预约管理")
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController extends BaseController {

    private final AppointmentService appointmentService;

    @Operation(summary = "预约挂号")
    @PostMapping("/book")
    public Result<Appointment> book(@Valid @RequestBody AppointmentBookRequest request) {
        return success(appointmentService.book(request));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel")
    public Result<String> cancel(@Valid @RequestBody AppointmentCancelRequest request) {
        appointmentService.cancel(request);
        return success("取消成功");
    }

    @Operation(summary = "预约详情")
    @GetMapping("/detail")
    public Result<Appointment> detail(@RequestParam String appointmentId) {
        return success(appointmentService.getDetail(appointmentId));
    }

    @Operation(summary = "预约列表")
    @GetMapping("/list")
    public Result<List<Appointment>> list(@RequestParam(required = false) String patientId,
                                          @RequestParam(required = false) String doctorId) {
        if (patientId != null && !patientId.isEmpty()) {
            return success(appointmentService.listByPatient(patientId));
        }
        if (doctorId != null && !doctorId.isEmpty()) {
            return success(appointmentService.listByDoctor(doctorId));
        }
        return fail("请提供 patientId 或 doctorId");
    }
}

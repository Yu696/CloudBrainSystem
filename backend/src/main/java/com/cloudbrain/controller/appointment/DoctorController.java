package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.service.appointment.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "医生管理")
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController extends BaseController {

    private final DoctorService doctorService;

    @Operation(summary = "医生列表")
    @GetMapping("/list")
    public Result<List<Doctor>> list(@RequestParam(required = false) String departmentId) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return success(doctorService.listByDepartment(departmentId));
        }
        return success(doctorService.listAll());
    }

    @Operation(summary = "医生详情")
    @GetMapping("/detail")
    public Result<Doctor> detail(@RequestParam String doctorId) {
        return success(doctorService.getDetail(doctorId));
    }
}

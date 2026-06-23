package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.DoctorUpdateRequest;
import com.cloudbrain.dto.response.DoctorVO;
import com.cloudbrain.service.appointment.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "医生管理")
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController extends BaseController {

    private final DoctorService doctorService;

    @Operation(summary = "医生列表")
    @GetMapping("/list")
    public Result<List<DoctorVO>> list(@RequestParam(required = false) String departmentId) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return success(doctorService.listByDepartment(departmentId));
        }
        return success(doctorService.listAll());
    }

    @Operation(summary = "医生详情")
    @GetMapping("/detail")
    public Result<DoctorVO> detail(@RequestParam String doctorId) {
        return success(doctorService.getDetail(doctorId));
    }

    @Operation(summary = "根据用户ID查询医生信息")
    @GetMapping("/get-by-user")
    public Result<DoctorVO> getByUserId(@RequestParam String userId) {
        return success(doctorService.getByUserId(userId));
    }

    @Operation(summary = "更新医生信息（专长、简介等）")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody DoctorUpdateRequest request) {
        doctorService.updateDoctor(request);
        return success("更新成功");
    }
}

package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.DoctorUpdateRequest;
import com.cloudbrain.dto.response.DoctorVO;
import com.cloudbrain.service.appointment.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "医生管理")
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController extends BaseController {

    private final DoctorService doctorService;

    @Operation(summary = "医生列表（仅可用）")
    @GetMapping("/list")
    public Result<List<DoctorVO>> list(@RequestParam(required = false) String departmentId) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return success(doctorService.listByDepartment(departmentId));
        }
        return success(doctorService.listAll());
    }

    @Operation(summary = "管理员医生列表（含不可用）")
    @GetMapping("/admin/list")
    public Result<List<DoctorVO>> adminList(@RequestParam(required = false) String departmentId) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return success(doctorService.adminListByDepartment(departmentId));
        }
        return success(doctorService.adminListAll());
    }

    @Operation(summary = "医生详情")
    @GetMapping("/detail")
    public Result<DoctorVO> detail(@RequestParam String doctorId) {
        return success(doctorService.getDetail(doctorId));
    }

    @Operation(summary = "当前登录医生信息")
    @GetMapping("/me")
    public Result<DoctorVO> me() {
        return success(doctorService.getCurrentDoctor());
    }

    @Operation(summary = "更新医生信息")
    @PutMapping("/update")
    public Result<String> update(@RequestParam String doctorId,
                                  @RequestBody DoctorUpdateRequest request) {
        doctorService.updateDoctor(doctorId, request);
        return success("更新成功");
    }

    @Operation(summary = "切换医生接诊状态")
    @PutMapping("/toggle")
    public Result<String> toggle(@RequestParam String doctorId) {
        doctorService.toggleAvailable(doctorId);
        return success("操作成功");
    }
}

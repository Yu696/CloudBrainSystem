package com.cloudbrain.controller.admin;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.response.PatientInfoVO;
import com.cloudbrain.entity.User;
import com.cloudbrain.service.patient.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员端")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final PatientService patientService;

    /** 管理员查看全部患者列表 */
    @Operation(summary = "管理员患者列表")
    @GetMapping("/patients")
    public Result<List<PatientInfoVO>> patients(@RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String phone,
                                                 @RequestParam(required = false) String medicalRecordNo) {
        checkAdmin();
        return success(patientService.listPatients(name, phone, medicalRecordNo));
    }

    private void checkAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User user = (User) auth.getPrincipal();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BusinessException("无权限，仅管理员可操作");
        }
    }
}

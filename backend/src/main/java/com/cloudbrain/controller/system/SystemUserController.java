package com.cloudbrain.controller.system;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.SystemUserAddRequest;
import com.cloudbrain.dto.request.SystemUserStatusRequest;
import com.cloudbrain.dto.request.SystemUserUpdateRequest;
import com.cloudbrain.dto.response.SystemUserVO;
import com.cloudbrain.service.system.SystemUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统管理")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemUserController extends BaseController {

    private final SystemUserService systemUserService;

    @Operation(summary = "新增系统用户")
    @PostMapping("/add")
    public Result<Map<String, String>> add(@Valid @RequestBody SystemUserAddRequest request) {
        String userId = systemUserService.addSystemUser(request);
        return success(Map.of("userId", userId));
    }

    @Operation(summary = "系统用户列表")
    @GetMapping("/list")
    public Result<List<SystemUserVO>> list() {
        return success(systemUserService.listSystemUsers());
    }

    @Operation(summary = "用户详情")
    @GetMapping("/detail")
    public Result<SystemUserVO> detail(@RequestParam String userId) {
        return success(systemUserService.getSystemUserDetail(userId));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/update")
    public Result<Map<String, Boolean>> update(@Valid @RequestBody SystemUserUpdateRequest request) {
        systemUserService.updateSystemUser(request);
        return success(Map.of("success", true));
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/status")
    public Result<Map<String, Boolean>> status(@Valid @RequestBody SystemUserStatusRequest request) {
        systemUserService.updateStatus(request);
        return success(Map.of("success", true));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    public Result<Map<String, Boolean>> delete(@RequestParam String userId) {
        systemUserService.deleteSystemUser(userId);
        return success(Map.of("success", true));
    }
}

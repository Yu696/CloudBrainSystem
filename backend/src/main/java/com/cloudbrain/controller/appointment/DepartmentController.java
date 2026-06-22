package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.DepartmentCreateRequest;
import com.cloudbrain.entity.Department;
import com.cloudbrain.service.appointment.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "科室管理")
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController extends BaseController {

    private final DepartmentService departmentService;

    @Operation(summary = "科室列表")
    @GetMapping("/list")
    public Result<List<Department>> list(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return success(departmentService.listByCategory(category));
        }
        return success(departmentService.listAll());
    }

    @Operation(summary = "新增科室")
    @PostMapping("/create")
    public Result<Department> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return success(departmentService.create(request));
    }

    @Operation(summary = "更新科室")
    @PutMapping("/update")
    public Result<String> update(@RequestParam String departmentId,
                                  @Valid @RequestBody DepartmentCreateRequest request) {
        departmentService.update(departmentId, request);
        return success("更新成功");
    }

    @Operation(summary = "删除科室")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String departmentId) {
        departmentService.delete(departmentId);
        return success("删除成功");
    }
}

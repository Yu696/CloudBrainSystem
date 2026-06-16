package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.entity.Department;
import com.cloudbrain.service.appointment.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

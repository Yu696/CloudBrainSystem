package com.cloudbrain.controller.medical;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.ExaminationOrderCreateRequest;
import com.cloudbrain.dto.request.ExaminationResultCreateRequest;
import com.cloudbrain.dto.response.ExaminationOrderVO;
import com.cloudbrain.dto.response.ExaminationResultVO;
import com.cloudbrain.service.medical.ExaminationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "诊疗记录 - 检查管理")
@RestController
@RequestMapping("/api/examination")
@RequiredArgsConstructor
public class ExaminationController extends BaseController {

    private final ExaminationService examinationService;

    @Operation(summary = "开检查单")
    @PostMapping("/create")
    public Result<ExaminationOrderVO> create(@Valid @RequestBody ExaminationOrderCreateRequest request) {
        return success(examinationService.createOrder(request));
    }

    @Operation(summary = "检查单详情")
    @GetMapping("/detail")
    public Result<ExaminationOrderVO> detail(@RequestParam String orderId) {
        return success(examinationService.getDetail(orderId));
    }

    @Operation(summary = "检查单列表")
    @GetMapping("/list")
    public Result<List<ExaminationOrderVO>> list(@RequestParam String recordId) {
        return success(examinationService.listOrders(recordId));
    }

    @Operation(summary = "更新检查单")
    @PutMapping("/update")
    public Result<ExaminationOrderVO> update(@RequestParam String orderId,
                                              @Valid @RequestBody ExaminationOrderCreateRequest request) {
        return success(examinationService.updateOrder(orderId, request));
    }

    @Operation(summary = "删除检查单")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String orderId) {
        examinationService.deleteOrder(orderId);
        return success("删除成功");
    }

    @Operation(summary = "影像检查单列表（医生端上传影像用）")
    @GetMapping("/imaging-orders")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RADIOLOGIST')")
    public Result<List<ExaminationOrderVO>> listImagingOrders(@RequestParam(required = false) String doctorId) {
        return success(examinationService.listImagingOrders(doctorId));
    }

    @Operation(summary = "全部检查单列表（检查医生端：所有检查类别）")
    @GetMapping("/all-orders")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RADIOLOGIST')")
    public Result<List<ExaminationOrderVO>> listAllOrders(@RequestParam(required = false) String doctorId) {
        return success(examinationService.listAllOrders(doctorId));
    }

    @Operation(summary = "检查结果")
    @GetMapping("/result")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RADIOLOGIST')")
    public Result<ExaminationResultVO> result(@RequestParam String orderId) {
        return success(examinationService.getResult(orderId));
    }

    @Operation(summary = "支付检查费（患者端钱包扣款）")
    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderId) {
        examinationService.payOrder(orderId);
        return success("支付成功");
    }

    @Operation(summary = "保存检查结果（新增或更新）")
    @PostMapping("/result")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RADIOLOGIST')")
    public Result<ExaminationResultVO> saveResult(@Valid @RequestBody ExaminationResultCreateRequest request) {
        return success(examinationService.saveResult(request));
    }
}

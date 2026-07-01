package com.cloudbrain.controller.medical;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.PrescriptionCreateRequest;
import com.cloudbrain.dto.response.PrescriptionVO;
import com.cloudbrain.service.medical.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "诊疗记录 - 处方管理")
@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController extends BaseController {

    private final PrescriptionService prescriptionService;

    @Operation(summary = "开具处方")
    @PostMapping("/create")
    public Result<PrescriptionVO> create(@Valid @RequestBody PrescriptionCreateRequest request) {
        return success(prescriptionService.createPrescription(request));
    }

    @Operation(summary = "处方列表")
    @GetMapping("/list")
    public Result<List<PrescriptionVO>> list(@RequestParam String recordId) {
        return success(prescriptionService.listPrescriptions(recordId));
    }

    @Operation(summary = "更新处方")
    @PutMapping("/update")
    public Result<PrescriptionVO> update(@RequestParam String prescriptionId,
                                          @Valid @RequestBody PrescriptionCreateRequest request) {
        return success(prescriptionService.updatePrescription(prescriptionId, request));
    }

    @Operation(summary = "删除处方")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String prescriptionId) {
        prescriptionService.deletePrescription(prescriptionId);
        return success("删除成功");
    }

    @Operation(summary = "处方详情")
    @GetMapping("/detail")
    public Result<PrescriptionVO> detail(@RequestParam String prescriptionId) {
        return success(prescriptionService.getPrescriptionDetail(prescriptionId));
    }

    @Operation(summary = "审核处方（待审核→已审核）")
    @PutMapping("/audit")
    public Result<String> audit(@RequestParam String prescriptionId) {
        prescriptionService.auditPrescription(prescriptionId);
        return success("审核成功");
    }

    @Operation(summary = "支付处方费用")
    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String prescriptionId) {
        prescriptionService.payOrder(prescriptionId);
        return success("支付成功");
    }
}

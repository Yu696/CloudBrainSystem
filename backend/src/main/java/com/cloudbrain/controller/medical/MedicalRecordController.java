package com.cloudbrain.controller.medical;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.MedicalRecordCreateRequest;
import com.cloudbrain.dto.request.MedicalRecordUpdateRequest;
import com.cloudbrain.dto.response.MedicalRecordVO;
import com.cloudbrain.service.medical.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "诊疗记录 - 病历管理")
@RestController
@RequestMapping("/api/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController extends BaseController {

    private final MedicalRecordService medicalRecordService;

    @Operation(summary = "创建病历")
    @PostMapping("/create")
    public Result<MedicalRecordVO> create(@Valid @RequestBody MedicalRecordCreateRequest request) {
        return success(medicalRecordService.createRecord(request));
    }

    @Operation(summary = "病历列表")
    @GetMapping("/list")
    public Result<List<MedicalRecordVO>> list(@RequestParam(required = false) String patientId,
                                               @RequestParam(required = false) String doctorId) {
        return success(medicalRecordService.listRecords(patientId, doctorId));
    }

    @Operation(summary = "病历详情")
    @GetMapping("/detail")
    public Result<MedicalRecordVO> detail(@RequestParam String recordId) {
        return success(medicalRecordService.getRecordDetail(recordId));
    }

    @Operation(summary = "更新病历")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody MedicalRecordUpdateRequest request) {
        medicalRecordService.updateRecord(request);
        return success("更新成功");
    }

    @Operation(summary = "完成病历")
    @PutMapping("/complete")
    public Result<String> complete(@RequestParam String recordId) {
        medicalRecordService.completeRecord(recordId);
        return success("病历已完成");
    }
}

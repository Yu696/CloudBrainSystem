package com.cloudbrain.controller.patient;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.PatientCreateRequest;
import com.cloudbrain.dto.request.PatientUpdateRequest;
import com.cloudbrain.dto.response.PatientCreateVO;
import com.cloudbrain.dto.response.PatientInfoVO;
import com.cloudbrain.service.patient.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "患者档案")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController extends BaseController {

    private final PatientService patientService;

    /** 创建患者档案，自动生成病历号，校验身份证唯一性 */
    @Operation(summary = "创建患者档案")
    @PostMapping("/create")
    public Result<PatientCreateVO> create(@Valid @RequestBody PatientCreateRequest request) {
        return success(patientService.createPatient(request));
    }

    /** 查询患者详细信息 */
    @Operation(summary = "查询患者信息")
    @GetMapping("/info")
    public Result<PatientInfoVO> info(@RequestParam String patientId) {
        return success(patientService.getPatientInfo(patientId));
    }

    /** 根据姓名、手机号、病历号搜索患者列表 */
    @Operation(summary = "患者列表")
    @GetMapping("/list")
    public Result<List<PatientInfoVO>> list(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String medicalRecordNo) {
        return success(patientService.listPatients(name, phone, medicalRecordNo));
    }

    /** 更新患者档案信息 */
    @Operation(summary = "更新患者档案")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody PatientUpdateRequest request) {
        patientService.updatePatient(request);
        return success("更新成功");
    }

    /** 校验身份证号是否已存在 */
    @Operation(summary = "校验身份证")
    @GetMapping("/check-idcard")
    public Result<Map<String, Boolean>> checkIdCard(@RequestParam String idCard) {
        boolean exists = patientService.checkIdCard(idCard);
        return success(Map.of("exists", exists));
    }
}

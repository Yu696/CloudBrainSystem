package com.cloudbrain.controller.pharmacy;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.request.pharmacy.DispenseRequest;
import com.cloudbrain.dto.request.pharmacy.DrugAddRequest;
import com.cloudbrain.dto.request.pharmacy.DrugUpdateRequest;
import com.cloudbrain.dto.request.pharmacy.StockAdjustRequest;
import com.cloudbrain.dto.request.pharmacy.DestroyExpiredRequest;
import com.cloudbrain.dto.request.pharmacy.TransferStockRequest;
import com.cloudbrain.dto.request.pharmacy.WarehouseRequest;
import com.cloudbrain.dto.response.pharmacy.DispenseResultVO;
import com.cloudbrain.dto.response.pharmacy.DrugVO;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;
import com.cloudbrain.dto.response.pharmacy.WarehouseVO;
import com.cloudbrain.dto.response.PrescriptionItemVO;
import com.cloudbrain.dto.response.PrescriptionVO;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.Prescription;
import com.cloudbrain.entity.PrescriptionItem;
import com.cloudbrain.entity.ShipRecord;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.Warehouse;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.PrescriptionItemMapper;
import com.cloudbrain.mapper.PrescriptionMapper;
import com.cloudbrain.mapper.ShipRecordMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.WarehouseMapper;
import com.cloudbrain.service.pharmacy.DispenseService;
import com.cloudbrain.service.pharmacy.DrugService;
import com.cloudbrain.service.pharmacy.InventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 药库管理 Controller — M07 药库管理
 * 10 个药库管理 API
 */
@Tag(name = "药库管理")
@RestController
@RequestMapping("/api/drug")
@RequiredArgsConstructor
public class DrugController extends BaseController {

    private final DrugService drugService;
    private final InventoryService inventoryService;
    private final DispenseService dispenseService;
    private final ShipRecordMapper shipRecordMapper;
    private final WarehouseMapper warehouseMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final UserMapper userMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;

    // ==================== DR-01 药品录入 ====================

    @Operation(summary = "新增药品（DR-01）")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody DrugAddRequest request) {
        String drugId = drugService.add(request);
        return success(drugId);
    }

    // ==================== DR-02 药品修改 ====================

    @Operation(summary = "修改药品（DR-02）")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody DrugUpdateRequest request) {
        drugService.update(request);
        return success("修改成功");
    }

    // ==================== DR-03 药品删除 ====================

    @Operation(summary = "删除药品（DR-03）")
    @DeleteMapping("/delete")
    public Result<String> delete(@Parameter(description = "药品 ID") @RequestParam String drugId) {
        drugService.delete(drugId);
        return success("删除成功");
    }

    // ==================== DR-04 药品搜索 ====================

    @Operation(summary = "获取所有可用药品列表（下拉选择用）")
    @GetMapping("/all")
    public Result<List<DrugVO>> allDrugs() {
        List<DrugVO> all = drugService.all();
        return success(all);
    }

    @Operation(summary = "搜索药品（DR-04）")
    @GetMapping("/search")
    public Result<PageResult<DrugVO>> search(
            @Parameter(description = "按名称/编码模糊搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "按药品分类筛选") @RequestParam(required = false) String category,
            @Parameter(description = "处方类型：0=OTC 1=处方药") @RequestParam(required = false) Integer prescriptionType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return success(drugService.search(keyword, category, prescriptionType, page, pageSize));
    }

    @Operation(summary = "药品详情")
    @GetMapping("/detail")
    public Result<DrugVO> detail(@Parameter(description = "药品 ID") @RequestParam String drugId) {
        return success(drugService.getDetail(drugId));
    }

    // ==================== DR-05 库存查询 ====================

    @Operation(summary = "查询库存（DR-05）")
    @GetMapping("/stock")
    public Result<StockVO> stock(@Parameter(description = "药品 ID") @RequestParam String drugId) {
        return success(inventoryService.getStock(drugId));
    }

    @Operation(summary = "库存列表（分页）")
    @GetMapping("/stock/list")
    public Result<PageResult<StockVO>> stockList(
            @Parameter(description = "仓库 ID") @RequestParam(required = false) String warehouseId,
            @Parameter(description = "关键词（药品ID/名称/编码）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return success(inventoryService.listByWarehouse(warehouseId, keyword, page, pageSize));
    }

    @Operation(summary = "销毁过期药品（库存置 0）")
    @PutMapping("/stock/destroy-expired")
    public Result<String> destroyExpired(@Valid @RequestBody DestroyExpiredRequest request) {
        inventoryService.destroyExpired(request.getDrugId(), request.getWarehouseId(), request.getBatchNo());
        return success("销毁成功");
    }

    @Operation(summary = "库存转移（调拨）")
    @PutMapping("/stock/transfer")
    public Result<String> transferStock(@Valid @RequestBody TransferStockRequest request) {
        inventoryService.transferStock(
                request.getDrugId(), request.getFromWarehouseId(),
                request.getToWarehouseId(), request.getQuantity(), request.getBatchNo());
        return success("转移成功");
    }

    // ==================== DR-06 库存预警 ====================

    @Operation(summary = "库存预警（DR-06）")
    @GetMapping("/low-stock")
    public Result<List<StockAlertVO>> lowStock(
            @Parameter(description = "0=低库存 1=过期预警 2=库存积压，不传=全部") @RequestParam(required = false) Integer type) {
        return success(inventoryService.listAlerts(type));
    }

    @Operation(summary = "标记预警已处理")
    @PutMapping("/alert/handle")
    public Result<String> handleAlert(@Parameter(description = "预警 ID") @RequestParam Long alertId) {
        inventoryService.handleAlert(alertId);
        return success("处理成功");
    }

    @Operation(summary = "删除预警记录")
    @DeleteMapping("/alert/{alertId}")
    public Result<String> deleteAlert(@Parameter(description = "预警 ID") @PathVariable Long alertId) {
        inventoryService.deleteAlert(alertId);
        return success("删除成功");
    }

    // ==================== DR-07 发药出库 ====================

    @Operation(summary = "发药出库（DR-07）")
    @PostMapping("/dispense")
    public Result<DispenseResultVO> dispense(@Valid @RequestBody DispenseRequest request) {
        return success(dispenseService.dispense(request));
    }

    // ==================== DR-07b 库存调整 ====================

    @Operation(summary = "调整库存（正数入库，负数出库）")
    @PutMapping("/stock/adjust")
    public Result<String> adjustStock(@Valid @RequestBody StockAdjustRequest request) {
        inventoryService.adjustStock(request.getDrugId(), request.getQuantity(),
                request.getWarehouseId(), request.getBatchNo(),
                request.getProductionDate(), request.getExpiryDate(),
                request.getMinStock(), request.getMaxStock());
        return success("库存调整成功");
    }

    // ==================== DR-08 取药单打印 ====================

    @Operation(summary = "取药单打印（DR-08）")
    @GetMapping("/print/{recordId}")
    public Result<ShipRecord> printShipRecord(
            @Parameter(description = "出药记录 ID") @PathVariable String recordId) {
        ShipRecord record = shipRecordMapper.selectOne(
                new LambdaQueryWrapper<ShipRecord>().eq(ShipRecord::getRecordId, recordId));
        if (record == null) {
            return fail("取药单不存在");
        }
        return success(record);
    }

    // ==================== DR-09 仓库管理 ====================

    @Operation(summary = "仓库列表（DR-09）")
    @GetMapping("/warehouse")
    public Result<List<WarehouseVO>> warehouseList() {
        List<Warehouse> warehouses = warehouseMapper.selectList(
                new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, 1));
        List<WarehouseVO> vos = warehouses.stream().map(w -> WarehouseVO.builder()
                .warehouseId(w.getWarehouseId())
                .name(w.getName())
                .location(w.getLocation())
                .adminId(w.getAdminId())
                .type(w.getType())
                .typeName(w.getType() == 0 ? "药库" : "药房")
                .status(w.getStatus())
                .createTime(w.getCreateTime())
                .build()).collect(Collectors.toList());
        return success(vos);
    }

    @Operation(summary = "新增仓库（DR-09）")
    @PostMapping("/warehouse")
    public Result<String> addWarehouse(@Valid @RequestBody WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseId(com.cloudbrain.util.UUIDUtil.generateWarehouseId());
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setAdminId(request.getAdminId());
        warehouse.setType(request.getType());
        warehouse.setStatus(1);
        warehouseMapper.insert(warehouse);
        return success(warehouse.getWarehouseId());
    }

    @Operation(summary = "更新仓库")
    @PutMapping("/warehouse")
    public Result<String> updateWarehouse(
            @Parameter(description = "仓库 ID") @RequestParam String warehouseId,
            @Valid @RequestBody WarehouseRequest request) {
        Warehouse warehouse = warehouseMapper.selectOne(
                new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getWarehouseId, warehouseId));
        if (warehouse == null) {
            return fail("仓库不存在");
        }
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        if (request.getAdminId() != null) warehouse.setAdminId(request.getAdminId());
        warehouse.setType(request.getType());
        warehouseMapper.updateById(warehouse);
        return success("更新成功");
    }

    @Operation(summary = "删除仓库")
    @DeleteMapping("/warehouse")
    public Result<String> deleteWarehouse(@Parameter(description = "仓库 ID") @RequestParam String warehouseId) {
        Warehouse warehouse = warehouseMapper.selectOne(
                new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getWarehouseId, warehouseId));
        if (warehouse == null) {
            return fail("仓库不存在");
        }
        warehouse.setStatus(0);
        warehouseMapper.updateById(warehouse);
        return success("删除成功");
    }

    // ==================== 发药订单列表 ====================

    @Operation(summary = "待发药处方列表")
    @GetMapping("/dispense/list")
    public Result<List<Map<String, Object>>> dispenseList() {
        // 查询所有已完成的处方（status=2），排除已发药的
        List<Prescription> prescriptions = prescriptionMapper.selectList(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getStatus, 2)
                        .orderByDesc(Prescription::getCreateTime));

        // 获取已发药的处方ID集合
        List<ShipRecord> shipped = shipRecordMapper.selectList(new LambdaQueryWrapper<>());
        java.util.Set<String> shippedPrescriptionIds = shipped.stream()
                .map(ShipRecord::getPrescriptionId)
                .collect(java.util.stream.Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Prescription p : prescriptions) {
            if (shippedPrescriptionIds.contains(p.getPrescriptionId())) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("prescriptionId", p.getPrescriptionId());
            item.put("patientId", p.getPatientId());
            item.put("doctorId", p.getDoctorId());
            item.put("prescriptionDesc", p.getPrescriptionDesc());
            item.put("totalAmount", p.getTotalAmount());
            item.put("status", p.getStatus());
            item.put("createTime", p.getCreateTime());

            // 患者姓名 — prescription.patientId 是 Patient 表的 patient_id
            Patient pat = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, p.getPatientId()));
            item.put("patientName", pat != null ? pat.getName() : p.getPatientId());

            // 医生姓名 — prescription.doctorId 是 Doctor 表的 doctor_id
            Doctor doc = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, p.getDoctorId()));
            if (doc != null) {
                User docUser = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getUserId, doc.getUserId()));
                item.put("doctorName", docUser != null ? docUser.getRealName() : doc.getDoctorId());
            } else {
                item.put("doctorName", p.getDoctorId());
            }

            // 处方明细
            List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                    new LambdaQueryWrapper<PrescriptionItem>()
                            .eq(PrescriptionItem::getPrescriptionId, p.getPrescriptionId()));
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (PrescriptionItem pi : items) {
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("drugId", pi.getDrugId());
                im.put("drugName", pi.getDrugName());
                im.put("spec", pi.getSpec());
                im.put("quantity", pi.getQuantity());
                im.put("unitPrice", pi.getUnitPrice());
                im.put("subtotal", pi.getSubtotal());
                itemList.add(im);
            }
            item.put("items", itemList);
            result.add(item);
        }
        return success(result);
    }

    @Operation(summary = "已发药记录列表")
    @GetMapping("/dispense/records")
    public Result<List<Map<String, Object>>> dispenseRecords() {
        List<ShipRecord> records = shipRecordMapper.selectList(
                new LambdaQueryWrapper<ShipRecord>().orderByDesc(ShipRecord::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ShipRecord r : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordId", r.getRecordId());
            item.put("prescriptionId", r.getPrescriptionId());
            item.put("patientId", r.getPatientId());
            item.put("shipNum", r.getShipNum());
            item.put("payAmount", r.getPayAmount());
            item.put("shipTime", r.getShipTime());
            item.put("printStatus", r.getPrintStatus());

            // 患者姓名 — ship_record.patientId 是 Patient 表的 patient_id
            Patient pat = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, r.getPatientId()));
            item.put("patientName", pat != null ? pat.getName() : r.getPatientId());

            // 通过处方查医生（prescription.doctorId 是 Doctor 表的 doctor_id）
            Prescription pres = prescriptionMapper.selectOne(
                    new LambdaQueryWrapper<Prescription>().eq(Prescription::getPrescriptionId, r.getPrescriptionId()));
            if (pres != null) {
                Doctor doc = doctorMapper.selectOne(
                        new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, pres.getDoctorId()));
                if (doc != null) {
                    User docUser = userMapper.selectOne(
                            new LambdaQueryWrapper<User>().eq(User::getUserId, doc.getUserId()));
                    item.put("doctorName", docUser != null ? docUser.getRealName() : doc.getDoctorId());
                } else {
                    item.put("doctorName", pres.getDoctorId());
                }
            } else {
                item.put("doctorName", "-");
            }
            result.add(item);
        }
        return success(result);
    }
}

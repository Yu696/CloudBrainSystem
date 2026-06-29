package com.cloudbrain.controller.pharmacy;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.request.pharmacy.DispenseRequest;
import com.cloudbrain.dto.request.pharmacy.DrugAddRequest;
import com.cloudbrain.dto.request.pharmacy.DrugUpdateRequest;
import com.cloudbrain.dto.request.pharmacy.WarehouseRequest;
import com.cloudbrain.dto.response.pharmacy.DispenseResultVO;
import com.cloudbrain.dto.response.pharmacy.DrugVO;
import com.cloudbrain.dto.response.pharmacy.StockAlertVO;
import com.cloudbrain.dto.response.pharmacy.StockVO;
import com.cloudbrain.dto.response.pharmacy.WarehouseVO;
import com.cloudbrain.entity.ShipRecord;
import com.cloudbrain.entity.Warehouse;
import com.cloudbrain.mapper.ShipRecordMapper;
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

import java.util.List;
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

    // ==================== DR-07 发药出库 ====================

    @Operation(summary = "发药出库（DR-07）")
    @PostMapping("/dispense")
    public Result<DispenseResultVO> dispense(@Valid @RequestBody DispenseRequest request) {
        return success(dispenseService.dispense(request));
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
}

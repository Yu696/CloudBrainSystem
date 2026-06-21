package com.cloudbrain.controller.appointment;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.ScheduleCreateRequest;
import com.cloudbrain.entity.Schedule;
import com.cloudbrain.entity.TimeSlot;
import com.cloudbrain.service.appointment.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "排班管理")
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController extends BaseController {

    private final ScheduleService scheduleService;

    @Operation(summary = "设置排班")
    @PostMapping("/set")
    public Result<Schedule> set(@Valid @RequestBody ScheduleCreateRequest request) {
        return success(scheduleService.create(request));
    }

    @Operation(summary = "查询排班")
    @GetMapping("/query")
    public Result<List<Schedule>> query(@RequestParam String doctorId,
                                        @RequestParam(required = false) LocalDate startDate,
                                        @RequestParam(required = false) LocalDate endDate) {
        return success(scheduleService.query(doctorId, startDate, endDate));
    }

    @Operation(summary = "可用时段")
    @GetMapping("/available")
    public Result<List<TimeSlot>> available(@RequestParam String doctorId,
                                            @RequestParam LocalDate date) {
        return success(scheduleService.getAvailableSlots(doctorId, date));
    }
}

package com.cloudbrain.service.appointment;

import com.cloudbrain.dto.request.ScheduleCreateRequest;
import com.cloudbrain.entity.Schedule;
import com.cloudbrain.entity.TimeSlot;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    Schedule create(ScheduleCreateRequest request);
    List<Schedule> query(String doctorId, LocalDate startDate, LocalDate endDate);
    List<TimeSlot> getAvailableSlots(String doctorId, LocalDate date);
}

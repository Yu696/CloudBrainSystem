package com.cloudbrain.service.appointment;

import com.cloudbrain.dto.request.AppointmentBookRequest;
import com.cloudbrain.dto.request.AppointmentCancelRequest;
import com.cloudbrain.dto.response.AppointmentVO;
import com.cloudbrain.entity.Appointment;
import java.util.List;

public interface AppointmentService {
    Appointment book(AppointmentBookRequest request);
    void cancel(AppointmentCancelRequest request);
    Appointment getDetail(String appointmentId);
    List<Appointment> listByPatient(String patientId);
    List<Appointment> listByDoctor(String doctorId);
    List<AppointmentVO> listAll(String patientId, String doctorId, String date, Integer status);
    void delete(String appointmentId);
}

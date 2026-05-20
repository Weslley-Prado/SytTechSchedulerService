package com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.api.AppointmentsApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.AppointmentDetails;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.AppointmentResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.ConfirmAppointmentRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.RescheduleRequest;
import com.syttech.syttech.scheduler.scheduler.ports.in.CancelAppointmentUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ConfirmAppointmentUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.GetAppointmentByCodeUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.RescheduleAppointmentUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ResendAppointmentCodeUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppointmentsController implements AppointmentsApi {

    private final ConfirmAppointmentUseCase confirmAppointment;
    private final CancelAppointmentUseCase cancelAppointment;
    private final RescheduleAppointmentUseCase rescheduleAppointment;
    private final ResendAppointmentCodeUseCase resendCode;
    private final GetAppointmentByCodeUseCase getByCode;

    public AppointmentsController(
            final ConfirmAppointmentUseCase confirmAppointment,
            final CancelAppointmentUseCase cancelAppointment,
            final RescheduleAppointmentUseCase rescheduleAppointment,
            final ResendAppointmentCodeUseCase resendCode,
            final GetAppointmentByCodeUseCase getByCode) {
        this.confirmAppointment = confirmAppointment;
        this.cancelAppointment = cancelAppointment;
        this.rescheduleAppointment = rescheduleAppointment;
        this.resendCode = resendCode;
        this.getByCode = getByCode;
    }

    @Override
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            final ConfirmAppointmentRequest body, final String idempotencyKey) {
        var appointment =
                confirmAppointment.confirmAppointment(
                        AppointmentsMapper.toCommand(body, idempotencyKey));
        return ResponseEntity.status(201).body(AppointmentsMapper.toResponse(appointment));
    }

    @Override
    public ResponseEntity<Void> cancelAppointment(final UUID appointmentId) {
        cancelAppointment.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            final UUID appointmentId, final RescheduleRequest body) {
        var updated =
                rescheduleAppointment.rescheduleAppointment(
                        appointmentId, AppointmentsMapper.toCommand(body));
        return ResponseEntity.ok(AppointmentsMapper.toResponse(updated));
    }

    @Override
    public ResponseEntity<Void> resendAppointmentCode(final UUID appointmentId) {
        resendCode.resendAppointmentCode(appointmentId);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<AppointmentDetails> getAppointmentByCode(final String code) {
        return ResponseEntity.ok(
                AppointmentsMapper.toDetails(getByCode.getAppointmentByCode(code)));
    }
}

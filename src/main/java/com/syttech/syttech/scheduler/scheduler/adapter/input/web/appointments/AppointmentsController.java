package com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.api.AppointmentsApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.AppointmentDetails;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.AppointmentResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.ConfirmAppointmentRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.RescheduleRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for AppointmentsApi. Replace each method body with a call to the corresponding
 * use case (ports.in) as soon as it is implemented.
 */
@RestController
public class AppointmentsController implements AppointmentsApi {

    @Override
    public ResponseEntity<Void> cancelAppointment(final UUID appointmentId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("cancelAppointment not implemented yet");
    }

    @Override
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            final ConfirmAppointmentRequest confirmAppointmentRequest,
            final String idempotencyKey) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("confirmAppointment not implemented yet");
    }

    @Override
    public ResponseEntity<AppointmentDetails> getAppointmentByCode(final String code) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("getAppointmentByCode not implemented yet");
    }

    @Override
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            final UUID appointmentId, final RescheduleRequest rescheduleRequest) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("rescheduleAppointment not implemented yet");
    }

    @Override
    public ResponseEntity<Void> resendAppointmentCode(final UUID appointmentId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("resendAppointmentCode not implemented yet");
    }
}

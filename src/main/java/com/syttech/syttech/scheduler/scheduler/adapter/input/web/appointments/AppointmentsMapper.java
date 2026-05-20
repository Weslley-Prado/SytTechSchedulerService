package com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.AppointmentDetails;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.AppointmentResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.ConfirmAppointmentRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.CreateHoldRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.HoldResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.RescheduleRequest;
import com.syttech.syttech.scheduler.scheduler.domain.command.ConfirmAppointmentCommand;
import com.syttech.syttech.scheduler.scheduler.domain.command.CreateHoldCommand;
import com.syttech.syttech.scheduler.scheduler.domain.command.GuestCustomerData;
import com.syttech.syttech.scheduler.scheduler.domain.command.RescheduleCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;

/** DTO ↔ domain converters for Appointments + Holds APIs. */
final class AppointmentsMapper {

    private AppointmentsMapper() {}

    static CreateHoldCommand toCommand(final CreateHoldRequest req) {
        return new CreateHoldCommand(
                req.getUnitId(), req.getServiceId(),
                req.getProfessionalId(), req.getStart());
    }

    static HoldResponse toResponse(final Hold h) {
        return new HoldResponse()
                .holdId(h.id())
                .professionalId(h.professionalId())
                .start(h.start())
                .end(h.end())
                .expiresAt(h.expiresAt());
    }

    static ConfirmAppointmentCommand toCommand(
            final ConfirmAppointmentRequest req, final String idempotencyKey) {
        GuestCustomerData guest = null;
        if (req.getCustomer() != null) {
            var g = req.getCustomer();
            guest =
                    new GuestCustomerData(
                            g.getFullName(), g.getEmail(), g.getPhone(), g.getNotes());
        }
        return new ConfirmAppointmentCommand(req.getHoldId(), null, guest, idempotencyKey);
    }

    static AppointmentResponse toResponse(final Appointment a) {
        return new AppointmentResponse()
                .appointmentId(a.id())
                .code(a.code())
                .status(
                        com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto
                                .AppointmentStatus.valueOf(a.status().name()))
                .start(a.start())
                .end(a.end());
    }

    static AppointmentDetails toDetails(final Appointment a) {
        return new AppointmentDetails()
                .appointmentId(a.id())
                .code(a.code())
                .status(
                        com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto
                                .AppointmentStatus.valueOf(a.status().name()))
                .start(a.start())
                .end(a.end())
                .unitId(a.unitId())
                .serviceId(a.serviceId())
                .professionalId(a.professionalId());
    }

    static RescheduleCommand toCommand(final RescheduleRequest req) {
        return new RescheduleCommand(req.getHoldId(), req.getStart());
    }
}

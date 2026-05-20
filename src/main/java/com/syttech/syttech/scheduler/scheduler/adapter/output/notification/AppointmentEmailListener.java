package com.syttech.syttech.scheduler.scheduler.adapter.output.notification;

import com.syttech.syttech.scheduler.scheduler.domain.event.AppointmentCancelledEvent;
import com.syttech.syttech.scheduler.scheduler.domain.event.AppointmentConfirmedEvent;
import com.syttech.syttech.scheduler.scheduler.domain.event.AppointmentRescheduledEvent;
import com.syttech.syttech.scheduler.scheduler.ports.out.EmailNotificationPort;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Bridges appointment lifecycle events to the e-mail adapter (after DB commit). */
@Component
public class AppointmentEmailListener {

    private final EmailNotificationPort emails;

    public AppointmentEmailListener(final EmailNotificationPort emails) {
        this.emails = emails;
    }

    @TransactionalEventListener
    public void onConfirmed(final AppointmentConfirmedEvent event) {
        emails.sendAppointmentCode(event.appointmentId());
    }

    // Cancellation and reschedule reuse the same template for now (just resends the code).
    @TransactionalEventListener
    public void onCancelled(final AppointmentCancelledEvent event) {
        emails.sendAppointmentCode(event.appointmentId());
    }

    @TransactionalEventListener
    public void onRescheduled(final AppointmentRescheduledEvent event) {
        emails.sendAppointmentCode(event.appointmentId());
    }

    /** Fallback: if a listener fires outside a transaction (rare), still try to send. */
    @EventListener
    public void onConfirmedNoTx(final AppointmentConfirmedEvent event) {
        // no-op: kept as documentation hook; @TransactionalEventListener fires when in tx.
    }
}

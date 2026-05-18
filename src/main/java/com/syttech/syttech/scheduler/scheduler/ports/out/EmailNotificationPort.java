package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.UUID;

/** Outbound port used by domain listeners to dispatch transactional e-mails. */
public interface EmailNotificationPort {

    /** Sends the appointment confirmation with its public code. */
    void sendAppointmentCode(UUID appointmentId);

    /** Sends the e-mail verification link to a freshly registered Customer. */
    void sendCustomerVerification(UUID customerId, String token);
}

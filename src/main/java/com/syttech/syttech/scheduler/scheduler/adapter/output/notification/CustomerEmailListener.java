package com.syttech.syttech.scheduler.scheduler.adapter.output.notification;

import com.syttech.syttech.scheduler.scheduler.domain.event.CustomerRegisteredEvent;
import com.syttech.syttech.scheduler.scheduler.ports.out.EmailNotificationPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Dispatches e-mail verification on customer registration (after commit). */
@Component
public class CustomerEmailListener {

    private final EmailNotificationPort emails;

    public CustomerEmailListener(final EmailNotificationPort emails) {
        this.emails = emails;
    }

    @TransactionalEventListener
    public void onRegistered(final CustomerRegisteredEvent event) {
        emails.sendCustomerVerification(event.customerId(), event.verifyToken());
    }
}

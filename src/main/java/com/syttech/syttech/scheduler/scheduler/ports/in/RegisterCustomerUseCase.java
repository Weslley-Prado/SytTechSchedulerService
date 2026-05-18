package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.command.RegisterCustomerCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;

/** Creates a Customer account and triggers e-mail verification. */
public interface RegisterCustomerUseCase {

    Customer registerCustomer(RegisterCustomerCommand command);
}

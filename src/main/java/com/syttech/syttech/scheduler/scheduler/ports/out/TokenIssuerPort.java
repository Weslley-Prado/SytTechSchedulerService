package com.syttech.syttech.scheduler.scheduler.ports.out;

import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;

/** Issues authentication tokens (JWT) for an authenticated Customer. */
public interface TokenIssuerPort {

    LoginResult issueFor(Customer customer);
}

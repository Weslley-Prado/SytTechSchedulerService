package com.syttech.syttech.scheduler.scheduler.ports.in;

/** Confirms the Customer e-mail given a verification token. */
public interface VerifyCustomerEmailUseCase {

    void verifyCustomerEmail(String token);
}

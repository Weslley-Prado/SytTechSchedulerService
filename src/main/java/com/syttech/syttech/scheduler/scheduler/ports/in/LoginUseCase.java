package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.command.LoginCommand;
import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;

/** Authenticates a Customer and issues JWT tokens. */
public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}

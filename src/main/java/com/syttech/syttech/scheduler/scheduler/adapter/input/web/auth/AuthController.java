package com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.api.AuthApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.LoginRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.auth.dto.LoginResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for AuthApi. Replace each method body with a call to the corresponding use case
 * (ports.in) as soon as it is implemented.
 */
@RestController
public class AuthController implements AuthApi {

    @Override
    public ResponseEntity<LoginResponse> login(final LoginRequest loginRequest) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("login not implemented yet");
    }
}

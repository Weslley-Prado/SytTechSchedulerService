package com.syttech.syttech.scheduler.scheduler.adapter.output.security;

import com.syttech.syttech.scheduler.scheduler.ports.out.PasswordHasherPort;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(final String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(final String rawPassword, final String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return encoder.matches(rawPassword, storedHash);
    }
}

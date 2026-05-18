package com.syttech.syttech.scheduler.scheduler.ports.out;

/** Encapsulates the password hashing algorithm. */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String storedHash);
}

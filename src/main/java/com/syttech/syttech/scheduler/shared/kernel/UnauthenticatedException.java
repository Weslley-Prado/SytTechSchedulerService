package com.syttech.syttech.scheduler.shared.kernel;

/** Thrown by web layer when an endpoint requires authentication but none was provided. */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(final String message) {
        super(message);
    }
}

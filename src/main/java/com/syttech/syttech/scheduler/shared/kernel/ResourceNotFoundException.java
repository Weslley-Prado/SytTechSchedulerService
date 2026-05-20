package com.syttech.syttech.scheduler.shared.kernel;

/** Base para exceções de recurso não encontrado → 404 NOT FOUND. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}

package com.syttech.syttech.scheduler.shared.kernel;

/** Base para exceções de recurso não encontrado → 404 NOT FOUND. */
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}

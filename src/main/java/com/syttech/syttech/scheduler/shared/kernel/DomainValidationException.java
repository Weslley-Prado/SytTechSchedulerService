package com.syttech.syttech.scheduler.shared.kernel;

/** Base para exceções de validação de domínio → 422 UNPROCESSABLE ENTITY. */
public class DomainValidationException extends RuntimeException {
    public DomainValidationException(final String message) {
        super(message);
    }
}

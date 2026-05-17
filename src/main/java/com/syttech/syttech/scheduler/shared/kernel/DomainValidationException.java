package com.syttech.syttech.scheduler.shared.kernel;

/** Base para exceções de validação de domínio → 422 UNPROCESSABLE ENTITY. */
public abstract class DomainValidationException extends RuntimeException {
    protected DomainValidationException(String message) {
        super(message);
    }
}

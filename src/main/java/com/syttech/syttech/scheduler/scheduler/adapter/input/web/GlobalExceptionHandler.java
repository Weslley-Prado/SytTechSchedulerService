package com.syttech.syttech.scheduler.scheduler.adapter.input.web;

import java.net.URI;
import jakarta.validation.ConstraintViolationException;

import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;
import com.syttech.syttech.scheduler.shared.kernel.UnauthenticatedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain and validation exceptions to RFC 7807 Problem Details responses.
 *
 * <ul>
 *   <li>{@link ResourceNotFoundException} → 404
 *   <li>{@link DomainValidationException} → 422
 *   <li>Bean Validation errors → 422
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(final ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthenticated(final UnauthenticatedException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(
            final DomainValidationException ex) {
        return problem(
                HttpStatus.UNPROCESSABLE_ENTITY, "Domain Validation Failed", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBeanValidation(
            final MethodArgumentNotValidException ex) {
        String detail =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(ex.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Failed", detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            final ConstraintViolationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Failed", ex.getMessage());
    }

    private ResponseEntity<ProblemDetail> problem(
            final HttpStatus status, final String title, final String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("about:blank"));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }
}

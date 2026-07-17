package com.jantabank.exception;

import com.jantabank.common.ApiResponse;
import com.jantabank.web.filter.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Central exception handler translating exceptions into the standardized
 * {@link ApiResponse} error envelope, with proper HTTP status codes and a
 * correlation id for traceability.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String path(WebRequest webRequest) {
        return webRequest.getDescription(false).replaceFirst("^uri=", "");
    }

    private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, String message,
                                                      String errorCode, List<String> errors, WebRequest webRequest) {
        ApiResponse<Object> body = ApiResponse.error(message, errorCode, errors, path(webRequest));
        body.setTraceId(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(TodoAPIException.class)
    public ResponseEntity<ApiResponse<Object>> handleTodoAPIException(TodoAPIException exception, WebRequest webRequest) {
        HttpStatus status = exception.getStatus() != null ? exception.getStatus() : HttpStatus.BAD_REQUEST;
        String errorCode;
        if (status == HttpStatus.UNAUTHORIZED) {
            errorCode = "UNAUTHORIZED";
        } else if (status == HttpStatus.LOCKED) {
            errorCode = "ACCOUNT_LOCKED";
        } else if (status == HttpStatus.FORBIDDEN) {
            errorCode = "FORBIDDEN";
        } else {
            errorCode = "BUSINESS_RULE_VIOLATION";
        }
        log.warn("Business rule violation ({}): {}", status.value(), exception.getMessage());
        return build(status, exception.getMessage(), errorCode, null, webRequest);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException exception, WebRequest webRequest) {
        log.warn("Resource not found: {}", exception.getMessage());
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), "RESOURCE_NOT_FOUND", null, webRequest);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException exception, WebRequest webRequest) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        log.warn("Validation failed: {}", errors);
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR", errors, webRequest);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException exception, WebRequest webRequest) {
        log.warn("Authentication failure: {}", exception.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", "AUTHENTICATION_FAILED", null, webRequest);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException exception, WebRequest webRequest) {
        log.warn("Access denied: {}", exception.getMessage());
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", "ACCESS_DENIED", null, webRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception exception, WebRequest webRequest) {
        log.error("Unhandled exception", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "INTERNAL_ERROR", null, webRequest);
    }
}

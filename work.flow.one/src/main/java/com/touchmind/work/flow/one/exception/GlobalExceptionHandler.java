package com.touchmind.work.flow.one.exception;

import com.touchmind.work.flow.one.dto.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Order(-2)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public Mono<ApiResponse> handleApiException(ApiException exception, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(exception.getStatus());
        return Mono.just(build(exception.getStatus(), exception.getMessage(), null));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ApiResponse> handleValidation(WebExchangeBindException exception, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        Map<String, String> errors = exception.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
                        (first, second) -> first));
        return Mono.just(build(HttpStatus.BAD_REQUEST, "Validation failed", errors));
    }

    @ExceptionHandler({ServerWebInputException.class, IllegalArgumentException.class})
    public Mono<ApiResponse> handleBadRequest(Exception exception, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return Mono.just(build(HttpStatus.BAD_REQUEST, exception.getMessage(), null));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ApiResponse> handleDuplicateKey(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.CONFLICT);
        return Mono.just(build(HttpStatus.CONFLICT, "Resource already exists", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ApiResponse> handleAccessDenied(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return Mono.just(build(HttpStatus.FORBIDDEN, "Access denied", null));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ApiResponse> handleUnexpected(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return Mono.just(build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", null));
    }

    private ApiResponse build(HttpStatus status, String message, Object data) {
        return new ApiResponse(Instant.now(), status.value(), message, data);
    }
}

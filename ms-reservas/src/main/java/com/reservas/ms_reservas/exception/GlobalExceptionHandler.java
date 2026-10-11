package com.reservas.ms_reservas.exception;

import com.reservas.commons.dto.ErrorDto;
import com.reservas.commons.exception.ApiException;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.InternalServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException exception, HttpServletRequest request) {
        if (exception.getStatus().is5xxServerError()) {
            log.error("{} {} failed: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        } else {
            log.warn("{} {} rejected: [{}] {}", request.getMethod(), request.getRequestURI(), exception.getCode(), exception.getMessage());
        }
        return build(exception.getStatus(), exception.getCode(), exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        String message = exception.getConstraintViolations().stream().findFirst()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .orElse("Request validation failed");
        log.warn("{} {} invalid: {}", request.getMethod(), request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, BadRequestException.CODE, message, request.getRequestURI());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class,
            PessimisticLockingFailureException.class})
    public ResponseEntity<Object> handleConcurrencyAndIntegrity(Exception exception, HttpServletRequest request) {
        log.warn("{} {} conflicted with the stored data: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return build(HttpStatus.CONFLICT, ConflictException.CODE,
                "The operation conflicts with the current state of the data, please retry", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, InternalServerException.CODE,
                "An unexpected error occurred", request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(GlobalExceptionHandler::describe)
                .orElse("Request validation failed");
        return badRequest(message, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + " " + error.getDefaultMessage()))
                .findFirst()
                .orElse("Request validation failed");
        return badRequest(message, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        String path = path(request);
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        String message = body instanceof ProblemDetail detail && detail.getDetail() != null
                ? detail.getDetail()
                : status.getReasonPhrase();
        if (status.is5xxServerError()) {
            log.error("Request to {} failed", path, ex);
            message = "An unexpected error occurred";
        } else {
            log.warn("Request to {} rejected: {}", path, ex.getMessage());
        }
        return build(status, code(status), message, path);
    }

    private ResponseEntity<Object> badRequest(String message, WebRequest request) {
        log.warn("Request to {} invalid: {}", path(request), message);
        return build(HttpStatus.BAD_REQUEST, BadRequestException.CODE, message, path(request));
    }

    private static String describe(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest ? servletRequest.getRequest().getRequestURI() : "";
    }

    private static String code(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> BadRequestException.CODE;
            case NOT_FOUND -> "RESOURCE_NOT_FOUND";
            case METHOD_NOT_ALLOWED -> "METHOD_NOT_ALLOWED";
            case NOT_ACCEPTABLE -> "NOT_ACCEPTABLE";
            case UNSUPPORTED_MEDIA_TYPE -> "UNSUPPORTED_MEDIA_TYPE";
            default -> status.is5xxServerError() ? InternalServerException.CODE : status.name();
        };
    }

    private static ResponseEntity<Object> build(HttpStatus status, String code, String message, String path) {
        ErrorDto body = new ErrorDto()
                .status(status.value())
                .code(code)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .path(path);
        return ResponseEntity.status(status).body(body);
    }
}

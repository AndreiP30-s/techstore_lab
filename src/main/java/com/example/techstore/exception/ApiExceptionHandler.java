package com.example.techstore.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> missing(NotFoundException e) {
        return businessError(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ConflictException e) {
        return businessError(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> invalid(ValidationException e) {
        return businessError(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(PriceCalculationException.class)
    public ResponseEntity<ErrorResponse> calculation(PriceCalculationException e) {
        log.error("Сбой расчёта цен", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getCode(), "Не удалось рассчитать цены");
    }

    @ExceptionHandler(DiscountConfigurationException.class)
    public ResponseEntity<ErrorResponse> discountConfiguration(DiscountConfigurationException e) {
        log.error("Проверьте подготовку данных скидки в db/02-java-support.sql", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getCode(), "Сервис скидок не настроен");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parameterType(MethodArgumentTypeMismatchException e) {
        ErrorCode code = ErrorCode.INVALID_PARAMETER_TYPE;
        String hint = switch (e.getName()) {
            case "size" -> "Укажите целое число от 1 до 100.";
            case "page" -> "Укажите целое число от 0.";
            default -> code.getHint();
        };
        return response(HttpStatus.BAD_REQUEST, code,
                "Параметр '" + e.getName() + "' имеет неверный тип", hint, List.of(), HttpHeaders.EMPTY);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParameter(MissingServletRequestParameterException e) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_PARAMETER,
                "Не указан обязательный параметр '" + e.getParameterName() + "'");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> malformed(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST, "Не удалось прочитать тело запроса");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> validation(BindException e) {
        ErrorCode code = ErrorCode.VALIDATION_FAILED;
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .sorted()
                .toList();
        return response(HttpStatus.BAD_REQUEST, code, "Ошибка проверки данных",
                code.getHint(), details, HttpHeaders.EMPTY);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> method(HttpRequestMethodNotSupportedException e) {
        ErrorCode code = ErrorCode.METHOD_NOT_ALLOWED;
        return response(HttpStatus.METHOD_NOT_ALLOWED, code, "HTTP-метод не поддерживается",
                code.getHint(), List.of(), e.getHeaders());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> mediaType(HttpMediaTypeNotSupportedException e) {
        ErrorCode code = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, code, "Тип содержимого запроса не поддерживается",
                code.getHint(), List.of(), e.getHeaders());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException e) {
        return error(HttpStatus.CONFLICT, ErrorCode.DATA_CONFLICT, "Операция нарушает ограничение целостности данных");
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ErrorResponse> databaseUnavailable(DataAccessResourceFailureException e) {
        log.error("Ошибка доступа к базе данных", e);
        return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.DATABASE_UNAVAILABLE, "База данных недоступна");
    }

    private ResponseEntity<ErrorResponse> businessError(HttpStatus status, ApiException e) {
        return response(status, e.getCode(), e.getMessage(), e.getHint(), List.of(), HttpHeaders.EMPTY);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, ErrorCode code, String message) {
        return response(status, code, message, code.getHint(), List.of(), HttpHeaders.EMPTY);
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status, ErrorCode code, String message, String hint,
            List<String> details, HttpHeaders headers) {
        if (status.is4xxClientError()) {
            log.warn("Ошибка запроса: код={}, статус={}", code, status.value());
        }
        return ResponseEntity.status(status).headers(headers)
                .body(new ErrorResponse(status.value(), code.name(), message, hint, details, Instant.now()));
    }
}

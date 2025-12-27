package org.phuongnq.analyzer.controller.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @InitBinder
    public void initBinder (WebDataBinder binder) {
        StringTrimmerEditor trimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, trimmerEditor);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, AuthenticationException.class})
    public ResponseEntity<APIResponse> handleValidationExceptions(Exception ex) {
        log.error("Validation exception: {}", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createAPIResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<APIResponse> handleMethodArgumentNotValidExceptions(
            MethodArgumentNotValidException ex) {
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        StringBuilder errorMessageBuilder = new StringBuilder();
        for (ObjectError objectError : allErrors) {
            if (objectError instanceof FieldError fieldError) {
                errorMessageBuilder
                        .append(fieldError.getField())
                        .append(" ")
                        .append(fieldError.getDefaultMessage())
                        .append("; ");
            } else {
                errorMessageBuilder
                        .append(objectError.getDefaultMessage())
                        .append("; ");
            }
        }
        String errorMessage = errorMessageBuilder.toString().trim();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createAPIResponse(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleInternalServerError(Exception ex) {
        log.error("Internal Server Error: {}", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createAPIResponse(ex.getMessage()));
    }

    private APIResponse createAPIResponse(String message) {
        return new APIResponse(false, message);
    }
}

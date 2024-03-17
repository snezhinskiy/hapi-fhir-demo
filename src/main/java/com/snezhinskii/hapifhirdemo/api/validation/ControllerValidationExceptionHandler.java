package com.snezhinskii.hapifhirdemo.api.validation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerValidationExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleValidationErrors(
        HandlerMethodValidationException ex
    ) {
        Map<String, String> errors = ex.getAllValidationResults().stream()
            .collect(Collectors.toMap(
                validation -> validation.getMethodParameter().getParameterName(),
                validation -> validation.getResolvableErrors().get(0).getDefaultMessage()
            ));

        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }
}

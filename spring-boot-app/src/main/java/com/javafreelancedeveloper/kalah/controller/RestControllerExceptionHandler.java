package com.javafreelancedeveloper.kalah.controller;

import com.javafreelancedeveloper.kalah.dto.ErrorDTO;
import com.javafreelancedeveloper.kalah.exception.HandledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String UNHANDLED_EXCEPTION_MSG = "An unexpected error has occurred. Please try again.";

    @ExceptionHandler
    public ResponseEntity<ErrorDTO> handleUnexpectedException(Exception exception, WebRequest webRequest) {
        log.error("Handling Exception in RestControllerExceptionHandler", exception);
        return new ResponseEntity<>(new ErrorDTO(UNHANDLED_EXCEPTION_MSG), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDTO> handleValidationException(HandledException exception, WebRequest webRequest) {
        log.error("Handling HandledException in RestControllerExceptionHandler", exception);
        return new ResponseEntity<>(new ErrorDTO(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}

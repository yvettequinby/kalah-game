package com.javafreelancedeveloper.kalah.controller;

import com.javafreelancedeveloper.kalah.exception.HandledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HandledException.class)
    public ModelAndView handleException(HandledException exception){
        log.error("Handling HandledException in ControllerExceptionHandler", exception);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", exception);
        modelAndView.addObject("errorTitle", exception.getMessage());
        modelAndView.setViewName("error");
        return modelAndView;
    }
}

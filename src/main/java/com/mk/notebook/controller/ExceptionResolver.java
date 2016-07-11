package com.mk.notebook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Fursov
 */
@ControllerAdvice
public class ExceptionResolver {

    private final static Logger LOG = LoggerFactory.getLogger(ExceptionResolver.class);

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus
    private ResponseEntity<String> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        try {
            return new ResponseEntity<>(getErrorsJson(result.getFieldErrors()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(String.format("Error building response from: %s", ex.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    private ResponseEntity<String> serviceError(Exception ex) {
        LOG.error(ex.getMessage(), ex);

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getErrorsJson(final List<FieldError> fieldErrorList) throws IOException {
        if (fieldErrorList != null && fieldErrorList.size() > 0) {
            final List<String> list = new ArrayList<>();
            for (FieldError error : fieldErrorList) {
                list.add(String.format("Field '%s' validation failed. '%s' rejected. Reason: %s",
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()));
            }

            return objectMapper.writeValueAsString(list);
        }

        return null;
    }

}
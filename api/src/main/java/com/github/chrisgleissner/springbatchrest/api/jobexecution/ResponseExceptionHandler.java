package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.batch.operations.BatchRuntimeException;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAnyException(Exception e, WebRequest request) {
        String message = e.getMessage();
        String causeMessage = "";
        if (e.getCause() != null)
            causeMessage = e.getCause().getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError apiError = new ApiError(status.toString(), message, e.getClass().getSimpleName(), causeMessage);
        return handleExceptionInternal(e, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BatchRuntimeException.class)
    protected ResponseEntity<Object> handleBatchRuntimeException(BatchRuntimeException e, WebRequest request) {
        Throwable cause = e.getCause();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (cause instanceof DuplicateJobException
                || cause instanceof JobExecutionAlreadyRunningException
                || cause instanceof JobInstanceAlreadyCompleteException)
            status = HttpStatus.CONFLICT;
        else if (cause instanceof JobParametersInvalidException
            || cause instanceof JobParametersNotFoundException)
            status = HttpStatus.BAD_REQUEST;
        else if (cause instanceof NoSuchJobException
            || cause instanceof NoSuchJobExecutionException
            || cause instanceof NoSuchJobInstanceException)
            status = HttpStatus.NOT_FOUND;

        ApiError apiError = new ApiError(status.toString(), cause.getMessage(), cause.getClass().getSimpleName(), e.getMessage());
        return handleExceptionInternal(e, apiError, new HttpHeaders(), status, request);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public class ApiError {
        String status;
        String message;
        String exception;
        String detail;
    }
}
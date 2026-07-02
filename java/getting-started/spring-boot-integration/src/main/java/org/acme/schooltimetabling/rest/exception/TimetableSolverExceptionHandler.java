package org.acme.schooltimetabling.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TimetableSolverExceptionHandler {

    @ExceptionHandler({TimetableSolverException.class})
    public ResponseEntity<ErrorInfo> handleTimetableSolverException(TimetableSolverException exception) {
        return new ResponseEntity<>(new ErrorInfo(exception.getJobId(), exception.getMessage()), exception.getStatus());
    }
}

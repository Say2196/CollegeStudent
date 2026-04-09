package com.example.demo.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class StudentGlobalExceptionHandler {


    @ExceptionHandler(value = ContactNumberInvalidException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleContactException(ContactNumberInvalidException ex){
        return ErrorResponse.create(ex, HttpStatus.CONFLICT,ex.getMessage());
    }

    @ExceptionHandler(value = NoSuchStudentFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoSuchStudentException(NoSuchStudentFound ex){
        return ErrorResponse.create(ex, HttpStatus.NOT_FOUND,ex.getMessage());
    }

    @ExceptionHandler(value = AlreadyUnregisteredException.class)
    @ResponseStatus(HttpStatus.ALREADY_REPORTED)
    public ErrorResponse handleAlreadyUnRegEx(AlreadyUnregisteredException ex){
        return ErrorResponse.create(ex,HttpStatus.ALREADY_REPORTED,ex.getMessage());
    }


//    @ExceptionHandler(value = Exception.class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ErrorResponse handleExceptions(Exception ex){
//        return ErrorResponse.create(ex,HttpStatus.CONFLICT,ex.getMessage());
//
//    }

}

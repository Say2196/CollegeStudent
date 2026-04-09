package com.example.demo.Exceptions;

public class NoSuchStudentFound extends RuntimeException{

    public NoSuchStudentFound(String message) {
        super(message);
    }
}

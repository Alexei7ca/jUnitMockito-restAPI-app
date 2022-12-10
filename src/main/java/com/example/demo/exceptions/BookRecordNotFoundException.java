package com.example.demo.exceptions;

//RuntimeException is the superclass of those exceptions that can be thrown during the normal operation of the Java Virtual Machine.
// RuntimeException and its subclasses are unchecked exceptions.
public class BookRecordNotFoundException extends RuntimeException {
    public BookRecordNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}




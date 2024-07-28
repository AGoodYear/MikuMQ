package com.ivmiku.mikumq.exception;

public class ErrorResponseException extends RuntimeException{
    public ErrorResponseException(String message) {
        super(message);
    }
}

package com.example.webApplication.Exception;

public class EmailNotVerifyException extends RuntimeException{
    public EmailNotVerifyException(String mesasge) {
        super(mesasge);
    }
}

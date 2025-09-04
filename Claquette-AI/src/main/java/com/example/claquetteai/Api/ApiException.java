package com.example.claquetteai.Api;

public class ApiException extends RuntimeException{
    public ApiException(String message){
        super(message);
    }
}

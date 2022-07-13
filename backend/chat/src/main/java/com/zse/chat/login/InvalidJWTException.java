package com.zse.chat.login;

public class InvalidJWTException extends RuntimeException {

    public InvalidJWTException(){
        super("Provided authentication token is invalid");
    }
}

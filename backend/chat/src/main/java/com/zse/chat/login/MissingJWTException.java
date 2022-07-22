package com.zse.chat.login;

public class MissingJWTException extends RuntimeException {

    public MissingJWTException(){
        super("Missing authentication token");
    }

}

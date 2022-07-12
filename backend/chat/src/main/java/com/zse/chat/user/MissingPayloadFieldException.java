package com.zse.chat.user;

public class MissingPayloadFieldException extends RuntimeException {

    public MissingPayloadFieldException(String fieldName){
        super("Required field: \"" + fieldName + "\"");
    }
}

package com.zse.chat.login;

public class MessageUpdateFailedException extends RuntimeException {

    public MessageUpdateFailedException(){
        super("Message update was not possible due to insufficient permissions.");
    }

}

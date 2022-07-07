package com.zse.chat.message;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(int id){
        super("Message not found. No Id: " + id);
    }
}

package com.zse.chat.user;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String nick){
        super("User with nick: \"" + nick + "\" already exists");
    }
}

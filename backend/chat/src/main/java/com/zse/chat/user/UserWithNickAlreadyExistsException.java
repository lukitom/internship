package com.zse.chat.user;

public class UserWithNickAlreadyExistsException extends RuntimeException {

    public UserWithNickAlreadyExistsException(String nick){
        super("User with nick: \"" + nick + "\" already exists");
    }
}

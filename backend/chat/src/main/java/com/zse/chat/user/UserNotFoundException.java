package com.zse.chat.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int id) {
        super("User not found. No Id: " + id);
    }

    public UserNotFoundException(String nick){
        super("User not found. No nick: " + nick);
    }
}

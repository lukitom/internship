package com.zse.chat.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int id) {
        super("User not found. No Id: " + id);
    }

    public UserNotFoundException(String nick){
        super("User not found. No nick: " + nick);
    }
}

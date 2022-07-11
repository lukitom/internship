package com.zse.chat.user;

public class UserWithEmailAlreadyExistsExeption extends RuntimeException {

    public UserWithEmailAlreadyExistsExeption(String email){
        super("User with email: \"" + email + "\" already exists" );
    }

}

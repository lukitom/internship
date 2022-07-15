package com.zse.chat.login;

public class ForbiddenMessageUpdateUserIsNotOwnerException extends RuntimeException {
    public ForbiddenMessageUpdateUserIsNotOwnerException(){
        super("Forbidden updating message. You didn't send this message.");
    }
}

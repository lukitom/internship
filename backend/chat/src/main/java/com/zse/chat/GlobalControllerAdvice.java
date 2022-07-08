package com.zse.chat;

import com.zse.chat.message.MessageNotFoundException;
import com.zse.chat.user.UserAlreadyExistsException;
import com.zse.chat.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserNotFoundException.class, MessageNotFoundException.class})
    public ExceptionResponse notFoundExceptionHandle(Exception notFoundException){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.NOT_FOUND.value())
                .exceptionMessage(notFoundException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ExceptionResponse userAlreadyExists(UserAlreadyExistsException userExistsException){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .exceptionMessage(userExistsException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}

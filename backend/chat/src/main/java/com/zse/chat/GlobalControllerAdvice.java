package com.zse.chat;

import com.zse.chat.message.MessageNotFoundException;
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
    public ExceptionResponse NotFoundExceptionHandle(Exception notFoundException){
        return ExceptionResponse.builder()
                .responseCode(404)
                .exceptionMessage(notFoundException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}

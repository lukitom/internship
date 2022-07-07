package com.zse.chat;

import com.zse.chat.message.ExceptionResponse;
import com.zse.chat.message.MessageNotFoundException;
import com.zse.chat.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler({UserNotFoundException.class, MessageNotFoundException.class})
    public ResponseEntity<Object> test(Exception notFoundException){
        var test = ExceptionResponse.builder()
                .responseCode(404)
                .exceptionMessage(notFoundException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(test, HttpStatus.NOT_FOUND);
    }
}

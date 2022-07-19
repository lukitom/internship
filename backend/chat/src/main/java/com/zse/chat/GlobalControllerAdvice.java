package com.zse.chat;

import com.zse.chat.channel.ChannelNotFoundException;
import com.zse.chat.channel.ChannelUpdateFailedException;
import com.zse.chat.login.MessageUpdateFailedException;
import com.zse.chat.login.InvalidJWTException;
import com.zse.chat.login.MissingJWTException;
import com.zse.chat.message.MessageNotFoundException;
import com.zse.chat.user.MissingPayloadFieldException;
import com.zse.chat.user.UserNotFoundException;
import com.zse.chat.user.UserWithEmailAlreadyExistsExeption;
import com.zse.chat.user.UserWithNickAlreadyExistsException;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserNotFoundException.class, MessageNotFoundException.class, ChannelNotFoundException.class})
    public ExceptionResponse notFoundExceptionHandle(Exception notFoundException){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.NOT_FOUND.value())
                .exceptionMessage(notFoundException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserWithNickAlreadyExistsException.class, UserWithEmailAlreadyExistsExeption.class})
    public ExceptionResponse userAlreadyExists(Exception userExistsException){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .exceptionMessage(userExistsException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingPayloadFieldException.class)
    public ExceptionResponse missingArgument(Exception missingArgumentException){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .exceptionMessage(missingArgumentException.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({MissingJWTException.class, InvalidJWTException.class})
    public ExceptionResponse requiredJWT(Exception exception){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.UNAUTHORIZED.value())
                .exceptionMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({MessageUpdateFailedException.class, ChannelUpdateFailedException.class})
    public ExceptionResponse notOwnerTryingUpdateMessage(Exception exception){
        return ExceptionResponse.builder()
                .responseCode(HttpStatus.FORBIDDEN.value())
                .exceptionMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Data
    @Builder
    static class ExceptionResponse {

        int responseCode;
        String exceptionMessage;
        LocalDateTime timestamp;

    }
}

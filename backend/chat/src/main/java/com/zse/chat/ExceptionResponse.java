package com.zse.chat;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ExceptionResponse {

    int responseCode;
    String exceptionMessage;
    LocalDateTime timestamp;

}

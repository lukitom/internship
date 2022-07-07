package com.zse.chat.message;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExceptionResponse {

    int responseCode;
    String exceptionMessage;
    LocalDateTime timestamp;

}

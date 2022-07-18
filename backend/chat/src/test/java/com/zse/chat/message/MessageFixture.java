package com.zse.chat.message;

import com.zse.chat.user.User;

import java.time.LocalDateTime;

public class MessageFixture {

    public static Message.MessageBuilder createDefaultMessage(int number, User user){
        return Message.builder()
                .id(1)
                .author(user)
                .content("testContent" + number)
                .createdAt(LocalDateTime.now());
    }

}

package com.zse.chat.message;

import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageFixture {

    public static Message.MessageBuilder createDefaultMessage(int number, User user){
        return Message.builder()
                .id(number)
                .author(user)
                .content("testContent" + number)
                .createdAt(LocalDateTime.now());
    }

    public static List<Message> createListOfMessages(int amount){
        return createListOfMessages(1, amount);
    }

    public static List<Message> createListOfMessages(int min, int amount){
        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < amount; i++){
            messages.add(createDefaultMessage(
                    i + min,
                    UserFixture.createDefaultUser(i + min).build()
            ).build());
        }

        return messages;
    }

}

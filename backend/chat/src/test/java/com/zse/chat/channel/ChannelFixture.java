package com.zse.chat.channel;

import com.zse.chat.message.Message;
import com.zse.chat.user.User;

import java.util.List;

public class ChannelFixture {

    public static Channel.ChannelBuilder createDefaultChannel(
            int number,
            List<User> owners,
            List<User> members,
            List<Message> messages
    ) {
        return Channel.builder()
                .id(number)
                .owners(owners)
                .members(members)
                .messages(messages);
    }
}

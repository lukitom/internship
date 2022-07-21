package com.zse.chat.message.channel;

public class ChannelAccessFailedException extends RuntimeException {

    public ChannelAccessFailedException() {
        super("Access channel was not possible due to insufficient permissions.");
    }
}

package com.zse.chat.channel;

public class ChannelUpdateFailedException extends RuntimeException {

    public ChannelUpdateFailedException() {
        super("Channel update was not possible due to insufficient permissions.");
    }

}

package com.zse.chat.channel;

public class ChannelNotFoundException extends RuntimeException {

    public ChannelNotFoundException(int id){
        super("Channel not found. No id: " + id);
    }

}

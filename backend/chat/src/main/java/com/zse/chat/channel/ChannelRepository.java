package com.zse.chat.channel;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends CrudRepository<Channel, Integer> {


    @Query(
            value = "SELECT channel.id FROM channel, user_member_channel, user_owner_channel, chat_user WHERE " +
                    "channel.id = user_member_channel.channel_id AND channel.id = user_owner_channel.channel_id AND " +
                    "chat_user.id = user_member_channel.user_id AND chat_user.id = user_owner_channel.user_id AND " +
                    "chat_user.nickname = :nickname",
            nativeQuery = true
    )
    List<Channel> getAvailableChannelsByUser(String nickname);

}

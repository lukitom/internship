package com.zse.chat.message;

import com.zse.chat.channel.Channel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message, Integer>{

    List<Message> findAllByDeletedFalseAndChannelIsOrderByIdAsc(Channel channel);

    List<Message> findAllByDeletedFalseAndChannelIsNullOrderByIdAsc();
}

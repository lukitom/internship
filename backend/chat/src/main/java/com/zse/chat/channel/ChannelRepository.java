package com.zse.chat.channel;

import com.zse.chat.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends CrudRepository<Channel, Integer> {

    List<Channel> getChannelsByOwnersInOrMembersIn(List<User> owners, List<User> members);

}

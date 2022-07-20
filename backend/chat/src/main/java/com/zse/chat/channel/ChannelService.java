package com.zse.chat.channel;

import com.zse.chat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    public List<Channel> getChannels(User user) {
        return channelRepository.getChannelsByOwnersInOrMembersIn(List.of(user), List.of(user));
    }

    public Channel getChannelById(int id){
        return channelRepository.findById(id).orElseThrow(() -> new ChannelNotFoundException(id));
    }

    public Channel findChannelById(int id){
        return channelRepository.findById(id).orElseThrow(() -> new ChannelNotFoundException(id));
    }

    public Channel saveChannel(User user){
        Channel channel = Channel.builder()
                .owners(List.of(user))
                .members(List.of())
                .build();

        return channelRepository.save(channel);
    }

    public void userHasPermissionToUpdateChannel(Channel channel, User user){
        channel.getOwners()
                .stream().filter(owner -> Objects.equals(owner.getNickname(), user.getNickname()))
                .findFirst().orElseThrow(ChannelUpdateFailedException::new);
    }

    public Channel updateChannel(Channel channel, ChannelUpdateAction action, User manipulateUser){
        List<User> owners = channel.getOwners();
        List<User> members = channel.getMembers();

        switch (action){
            case ADD_OWNER -> {
                owners.add(manipulateUser);
                members.remove(manipulateUser);
            }
            case REMOVE_OWNER -> {
                owners.remove(manipulateUser);
                members.add(manipulateUser);
            }
            case ADD_MEMBER -> {
                if(owners.contains(manipulateUser)){
                    return channel;
                }
                members.add(manipulateUser);
            }
            case REMOVE_MEMBER -> members.remove(manipulateUser);
        }

        Channel updatedChannel = Channel.builder()
                .id(channel.getId())
                .owners(owners)
                .members(members)
                .build();

        return channelRepository.save(updatedChannel);
    }
}

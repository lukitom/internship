package com.zse.chat.channel;

import com.zse.chat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Channel saveChannel(User user){
        final var channel = Channel.builder()
                .owners(List.of(user))
                .members(List.of())
                .build();

        return channelRepository.save(channel);
    }

    public boolean userHasPermissionToUpdateChannel(Channel channel, String nickname){
        final Optional<User> resultOwner = channel.getOwners()
                .stream().filter(owner -> owner.getNickname().equals(nickname))
                .findFirst();

        return resultOwner.isPresent();
    }

    public Channel updateChannel(Channel channel, ChannelUpdateAction action, User manipulateUser){
        final List<User> owners = channel.getOwners();
        final List<User> members = channel.getMembers();

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

        final var updatedChannel = Channel.builder()
                .id(channel.getId())
                .owners(owners)
                .members(members)
                .build();

        return channelRepository.save(updatedChannel);
    }

    public boolean userHasPermissionToSeeChannel(Channel channel, String nickname){
        final Optional<User> resultOwner = channel.getOwners()
                .stream()
                .filter(owner -> owner.getNickname().equals(nickname))
                .findFirst();

        final Optional<User> resultMember = channel.getMembers()
                .stream()
                .filter(member -> member.getNickname().equals(nickname))
                .findFirst();

        return resultOwner.isPresent() || resultMember.isPresent();
    }

}

package com.zse.chat.channel;

import com.zse.chat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    public Channel findChannelById(int id){
        return channelRepository.findById(id).orElseThrow(() -> new ChannelNotFoundException(id));
    }

    public Channel saveChannel(User user){
        List<User> owners = new ArrayList<>();
        List<User> members = new ArrayList<>();
        owners.add(user);

        Channel channel = Channel.builder()
                .owners(owners)
                .members(members)
                .build();

        return channelRepository.save(channel);
    }

    public Channel updateChannelMembers(ChannelController.ChannelUpdateDTO channelUpdateDTO, User user, boolean delete) {
        Channel previousChannel = findChannelById(channelUpdateDTO.getId());

        previousChannel.getOwners().stream().filter(e -> Objects.equals(e.getNickname(), channelUpdateDTO.getNickname()))
                .findFirst().orElseThrow(ChannelUpdateFailedException::new);

        List<User> owners = previousChannel.getOwners();
        List<User> members = previousChannel.getMembers();
        if(delete) {
            members.remove(user);
        } else {
            if(owners.contains(user)){
                return previousChannel;
            }
            members.add(user);
        }

        Channel updatedChannel = Channel.builder()
                .id(previousChannel.getId())
                .owners(owners)
                .members(members)
                .build();

        return channelRepository.save(updatedChannel);
    }

    public Channel updateChannelOwners(ChannelController.ChannelUpdateDTO channelUpdateDTO, User user, boolean delete) {
        Channel previousChannel = findChannelById(channelUpdateDTO.getId());

        previousChannel.getOwners().stream().filter(e -> Objects.equals(e.getNickname(), channelUpdateDTO.getNickname()))
                .findFirst().orElseThrow(ChannelUpdateFailedException::new);

        List<User> owners = previousChannel.getOwners();
        List<User> members = previousChannel.getMembers();
        if(delete) {
            owners.remove(user);
            members.add(user);
        } else {
            owners.add(user);
            members.remove(user);
        }

        Channel updatedChannel = Channel.builder()
                .id(previousChannel.getId())
                .owners(owners)
                .members(members)
                .build();

        return channelRepository.save(updatedChannel);
    }

}

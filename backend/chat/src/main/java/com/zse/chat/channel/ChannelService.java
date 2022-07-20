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

    public List<Channel> getChannels(ChannelController.ChannelRequestDTO channelRequestDTO) {
        return channelRepository.getAvailableChannelsByUser(channelRequestDTO.nickname);
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

    public Channel updateChannel(ChannelController.ChannelRequestDTO channelRequestDTO, User user, ChannelController.ChannelUpdateAction action){
        Channel previousChannelValue = findChannelById(channelRequestDTO.getId());

        previousChannelValue.getOwners().stream().filter(e -> Objects.equals(e.getNickname(), channelRequestDTO.getNickname()))
                .findFirst().orElseThrow(ChannelUpdateFailedException::new);

        List<User> owners = previousChannelValue.getOwners();
        List<User> members = previousChannelValue.getMembers();

        switch (action){
            case ADD_OWNER -> {
                owners.add(user);
                members.remove(user);
            }
            case REMOVE_OWNER -> {
                owners.remove(user);
                members.add(user);
            }
            case ADD_MEMBER -> {
                if(owners.contains(user)){
                    return previousChannelValue;
                }
                members.add(user);
            }
            case REMOVE_MEMBER -> members.remove(user);
        }


        Channel updatedChannel = Channel.builder()
                .id(previousChannelValue.getId())
                .owners(owners)
                .members(members)
                .build();

        return channelRepository.save(updatedChannel);
    }
}

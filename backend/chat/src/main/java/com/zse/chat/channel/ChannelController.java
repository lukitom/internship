package com.zse.chat.channel;

import com.zse.chat.login.VerifyJWT;
import com.zse.chat.user.User;
import com.zse.chat.user.UserNickname;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Channels")
@RestController("/channel")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final UserService userService;

    @Operation(summary = "Create new channel")
    @PostMapping
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public ChannelResponseDTO createChannel(ChannelCreateDTO channelCreateDTO){
        User user = userService.getUserByNick(channelCreateDTO.getNickname());
        Channel channel = channelService.saveChannel(user);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Add user to channel as member")
    @PostMapping("/member")
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public ChannelResponseDTO addUserAsMember(@RequestBody ChannelUpdateDTO channelUpdateDTO){
        User user = userService.getUserByNick(channelUpdateDTO.userNickname);
        Channel channel = channelService.updateChannelMembers(channelUpdateDTO, user, false);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Remove user from channel")
    @DeleteMapping("/member")
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public ChannelResponseDTO removeUserFromChannel(@RequestBody ChannelUpdateDTO channelUpdateDTO){
        User user = userService.getUserByNick(channelUpdateDTO.userNickname);
        Channel channel = channelService.updateChannelMembers(channelUpdateDTO, user, true);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Add new owner privilege")
    @PostMapping("/owner")
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public ChannelResponseDTO addUserAsOwner(@RequestBody ChannelUpdateDTO channelUpdateDTO){
        User user = userService.getUserByNick(channelUpdateDTO.userNickname);
        Channel channel = channelService.updateChannelOwners(channelUpdateDTO, user, false);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Remove owner privilege")
    @DeleteMapping("/owner")
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public ChannelResponseDTO removeUserOwnerPrivilege(@RequestBody ChannelUpdateDTO channelUpdateDTO){
        User user = userService.getUserByNick(channelUpdateDTO.userNickname);
        Channel channel = channelService.updateChannelOwners(channelUpdateDTO, user, true);

        return createChannelResponseDTO(channel);
    }

    //region DTOs
    @Data
    @Builder
    static class ChannelCreateDTO implements UserNickname{
        String nickname;
    }


    @Data
    @Builder
    static class ChannelUpdateDTO implements UserNickname {
        int id;
        String nickname;
        String userNickname;
    }

    @Data
    @Builder
    static class ChannelResponseDTO {
        int id;
        List<String> owners;
        List<String> members;
    }
    //endregion

    private ChannelResponseDTO createChannelResponseDTO(Channel channel){
        List<String> owners = new ArrayList<>();
        List<String> members = new ArrayList<>();

        channel.getOwners().forEach(user -> {
            String userDTO = user.getNickname();
            owners.add(userDTO);
        });

        channel.getMembers().forEach(user -> {
            String userDTO = user.getNickname();
            members.add(userDTO);
        });

        return ChannelResponseDTO.builder()
                .id(channel.id)
                .owners(owners)
                .members(members)
                .build();
    }

}

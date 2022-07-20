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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Channels")
@RequestMapping("/channels")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class ChannelController {

    private final ChannelService channelService;
    private final UserService userService;


    @Operation(summary = "Get available channels")
    @GetMapping
    @VerifyJWT
    public List<ChannelResponseDTO> getAvailableChannels(ChannelRequestDTO channelRequestDTO){
        List<Channel> channels = channelService.getChannels(channelRequestDTO);

        return null;
    }

    @Operation(summary = "Create new channel")
    @PostMapping
    @VerifyJWT
    public ChannelResponseDTO createChannel(ChannelRequestDTO channelCreateDTO){
        final var user = userService.getUserByNick(channelCreateDTO.getNickname());
        final var channel = channelService.saveChannel(user);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Add user to channel as member")
    @PostMapping("/member")
    @VerifyJWT
    public ChannelResponseDTO addUserAsMember(@RequestBody ChannelRequestDTO channelRequestDTO){
        final var user = userService.getUserByNick(channelRequestDTO.userNickname);
        final var channel = channelService.updateChannel(channelRequestDTO, user, ChannelUpdateAction.ADD_MEMBER);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Remove user from channel")
    @DeleteMapping("/member")
    @VerifyJWT
    public ChannelResponseDTO removeUserFromChannel(@RequestBody ChannelRequestDTO channelRequestDTO){
        final var user = userService.getUserByNick(channelRequestDTO.userNickname);
        final var channel = channelService.updateChannel(channelRequestDTO, user, ChannelUpdateAction.REMOVE_MEMBER);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Add new owner privilege")
    @PostMapping("/owner")
    @VerifyJWT
    public ChannelResponseDTO addUserAsOwner(@RequestBody ChannelRequestDTO channelRequestDTO){
        final var user = userService.getUserByNick(channelRequestDTO.userNickname);
        final var channel = channelService.updateChannel(channelRequestDTO, user, ChannelUpdateAction.ADD_OWNER);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Remove owner privilege")
    @DeleteMapping("/owner")
    @VerifyJWT
    public ChannelResponseDTO removeUserOwnerPrivilege(@RequestBody ChannelRequestDTO channelRequestDTO){
        final var user = userService.getUserByNick(channelRequestDTO.userNickname);
        final var channel = channelService.updateChannel(channelRequestDTO, user, ChannelUpdateAction.REMOVE_OWNER);

        return createChannelResponseDTO(channel);
    }

    //region DTOs
    @Data
    @Builder
    static class ChannelRequestDTO implements UserNickname {
        Integer id;
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
        return ChannelResponseDTO.builder()
                .id(channel.id)
                .owners(channel.getOwners().stream().map(User::getNickname).toList())
                .members(channel.getMembers().stream().map(User::getNickname).toList())
                .build();
    }

    enum ChannelUpdateAction {
        ADD_OWNER, REMOVE_OWNER, ADD_MEMBER, REMOVE_MEMBER
    }

}

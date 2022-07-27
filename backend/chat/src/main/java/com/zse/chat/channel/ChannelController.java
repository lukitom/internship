package com.zse.chat.channel;

import com.zse.chat.login.VerifyJWT;
import com.zse.chat.user.User;
import com.zse.chat.user.UserNickname;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Channels")
@RequestMapping("/channels")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Slf4j
public class ChannelController {

    private final ChannelService channelService;
    private final UserService userService;


    @Operation(summary = "Get available channels")
    @GetMapping
    @VerifyJWT
    public List<ChannelResponseDTO> getAvailableChannels(ChannelRequestDTO channelRequestDTO){
        final var user = userService.getUserByNick(channelRequestDTO.getNickname());
        final var channels = channelService.getChannels(user);

        return channels.stream()
                .map(this::createChannelResponseDTO).toList();
    }

    @Operation(summary = "Create new channel")
    @PostMapping
    @VerifyJWT
    public ChannelResponseDTO createChannel(ChannelRequestDTO channelCreateDTO){
        final var user = userService.getUserByNick(channelCreateDTO.getNickname());
        final var channel = channelService.saveChannel(user);

        log.info("Channel with id: {} has been created by {}", channel.getId(), user.getNickname());
        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Update users who have access to the channel and their permissions")
    @PutMapping("/users")
    @VerifyJWT
    public ChannelResponseDTO updateChannelUsers(@RequestBody ChannelRequestDTO channelRequestDTO){
        final var channel = channelService.getChannelById(channelRequestDTO.getId());

        final boolean hasPermission = channelService.userHasPermissionToUpdateChannel(channel, channelRequestDTO.getNickname());

        if (!hasPermission){
            throw new ChannelUpdateFailedException();
        }

        final var userToManipulate = userService.getUserByNick(channelRequestDTO.getUserNickname());
        final var updatedChannel = channelService.updateChannel(channel, channelRequestDTO.getAction(), userToManipulate);

        return createChannelResponseDTO(updatedChannel);
    }

    //region DTOs
    @Value
    @Builder
    @Jacksonized
    static class ChannelRequestDTO implements UserNickname {
        Integer id;
        @Setter
        @NonFinal
        String nickname;
        String userNickname;
        ChannelUpdateAction action;
    }

    @Builder
    @Jacksonized
    record ChannelResponseDTO(int id, List<String> owners, List<String> members) {
    }
    //endregion

    private ChannelResponseDTO createChannelResponseDTO(Channel channel){
        return ChannelResponseDTO.builder()
                .id(channel.getId())
                .owners(channel.getOwners().stream().map(User::getNickname).toList())
                .members(channel.getMembers().stream().map(User::getNickname).toList())
                .build();
    }

}

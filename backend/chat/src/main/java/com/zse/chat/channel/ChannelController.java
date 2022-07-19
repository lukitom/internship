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
        User user = userService.getUserByNick(channelCreateDTO.getNickname());
        Channel channel = channelService.saveChannel(user);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Add user to channel as member")
    @PostMapping("/member")
    @VerifyJWT
    public ChannelResponseDTO addUserAsMember(@RequestBody ChannelRequestDTO channelRequestDTO){
        User user = userService.getUserByNick(channelRequestDTO.userNickname);
        Channel channel = channelService.updateChannelMembers(channelRequestDTO, user, false);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Remove user from channel")
    @DeleteMapping("/member")
    @VerifyJWT
    public ChannelResponseDTO removeUserFromChannel(@RequestBody ChannelRequestDTO channelRequestDTO){
        User user = userService.getUserByNick(channelRequestDTO.userNickname);
        Channel channel = channelService.updateChannelMembers(channelRequestDTO, user, true);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Add new owner privilege")
    @PostMapping("/owner")
    @VerifyJWT
    public ChannelResponseDTO addUserAsOwner(@RequestBody ChannelRequestDTO channelRequestDTO){
        User user = userService.getUserByNick(channelRequestDTO.userNickname);
        Channel channel = channelService.updateChannelOwners(channelRequestDTO, user, false);

        return createChannelResponseDTO(channel);
    }

    @Operation(summary = "Remove owner privilege")
    @DeleteMapping("/owner")
    @VerifyJWT
    public ChannelResponseDTO removeUserOwnerPrivilege(@RequestBody ChannelRequestDTO channelRequestDTO){
        User user = userService.getUserByNick(channelRequestDTO.userNickname);
        Channel channel = channelService.updateChannelOwners(channelRequestDTO, user, true);

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
        List<String> owners = new ArrayList<>();
        List<String> members = new ArrayList<>();

        channel.getOwners().forEach(user -> {
            String userDTO  = user.getNickname();
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

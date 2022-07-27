package com.zse.chat.message.channel;

import com.zse.chat.channel.Channel;
import com.zse.chat.channel.ChannelService;
import com.zse.chat.login.VerifyJWT;
import com.zse.chat.message.Message;
import com.zse.chat.message.MessageController.MessageRequestDTO;
import com.zse.chat.message.MessageController.MessageResponseDTO;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Messages in channels", description = "Endpoints to messages in specified channels")
@RequestMapping("/messages/channels/{channelId}")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Slf4j
public class MessageInChannelController {

    private final MessageChannelService messageChannelService;
    private final UserService userService;
    private final ChannelService channelService;

    @Operation(
            summary = "Get all messages in channel",
            parameters = @Parameter(name = "channelId", description = "Channel Id")
    )
    @GetMapping
    @VerifyJWT
    public List<MessageResponseDTO> getMessagesInChannel(
            MessageRequestDTO messageRequestDTO,
            @PathVariable int channelId
    ) {
        final var channel = channelService.getChannelById(channelId);
        checkAccess(channel, messageRequestDTO.getNickname());

        final List<Message> messages = messageChannelService.getMessages(channel);

        return messages.stream()
                .map(this::createMessageResponseDTO).toList();
    }

    @Operation(
            summary = "Create new message in channel",
            parameters = @Parameter(name = "channelId", description = "Channel Id")
    )
    @PostMapping
    @VerifyJWT
    public MessageResponseDTO createMessage(
            @RequestBody MessageRequestDTO messageRequestDTO,
            @PathVariable int channelId
    ) {
        final var channel = channelService.getChannelById(channelId);
        checkAccess(channel, messageRequestDTO.getNickname());

        final var user = userService.getUserByNick(messageRequestDTO.getNickname());

        final var savedMessage = messageChannelService.saveMessage(messageRequestDTO, user, channel);

        return createMessageResponseDTO(savedMessage);
    }

    @Operation(
            summary = "Update message in channel by messageId",
            parameters = {
                    @Parameter(name = "channelId", description = "Channel Id"),
                    @Parameter(name = "messageId", description = "Message Id")
            }
    )
    @PutMapping("/{messageId}")
    @VerifyJWT
    public MessageResponseDTO updateMessage(
            MessageRequestDTO messageRequestDTO,
            @PathVariable int channelId,
            @PathVariable int messageId
    ) {
        final var channel = channelService.getChannelById(channelId);
        checkAccess(channel, messageRequestDTO.getNickname());

        final var updatedMessage = messageChannelService.updateMessage(messageId, messageRequestDTO, channel);
        return createMessageResponseDTO(updatedMessage);
    }

    @Operation(
            summary = "Delete message in channel by message id",
            parameters = {
                    @Parameter(name = "channelId", description = "Channel Id"),
                    @Parameter(name = "messageId", description = "Message Id")
            }
    )
    @DeleteMapping("/{messageId}")
    @VerifyJWT
    public void deleteMessage(
            MessageRequestDTO messageRequestDTO,
            @PathVariable int channelId,
            @PathVariable int messageId
    ) {
        final var channel = channelService.getChannelById(channelId);
        checkAccess(channel, messageRequestDTO.getNickname());

        messageChannelService.updateMessage(messageId, messageRequestDTO, channel, true);
        log.info("Message with id: {} has been deleted", messageId);
    }

    private MessageResponseDTO createMessageResponseDTO(Message message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .authorNick(message.getAuthor().getNickname())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private void checkAccess(Channel channel, String nickname) {
        final var hasPermission = channelService.userHasPermissionToSeeChannel(channel, nickname);
        if (!hasPermission) {
            throw new ChannelAccessFailedException();
        }
    }
}

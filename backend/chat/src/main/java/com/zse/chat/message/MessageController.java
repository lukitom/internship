package com.zse.chat.message;

import com.zse.chat.login.VerifyJWT;
import com.zse.chat.user.User;
import com.zse.chat.user.UserNickname;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Messages global", description = "Endpoints to messages in global channel")
@RequestMapping("/messages")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Operation(summary = "Get all messages in global channel")
    @GetMapping
    @VerifyJWT(withoutArgs = true)
    public List<MessageResponseDTO> getMessages(){
        return messageService.getAllMessagesInGlobalChannel().stream()
                .map(this::createMessageResponseDTO)
                .toList();
    }

    @Deprecated
    @Operation(
            summary = "Get message in global channel by Id",
            parameters = {@Parameter(name = "id", description = "Message Id")}
    )
    @GetMapping("/{id}")
    @VerifyJWT(withoutArgs = true)
    public MessageResponseDTO getMessageById(@PathVariable int id){
        final var message = messageService.getMessageById(id);

        return createMessageResponseDTO(message);
    }

    @Operation(summary = "Create new message in global channel")
    @PostMapping
    @VerifyJWT
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO messageRequestDTO){
        final var author = userService.getUserByNick(messageRequestDTO.getNickname());
        final var savedMessage = messageService.saveMessage(messageRequestDTO, author);

        return  createMessageResponseDTO(savedMessage);
    }

    @Operation(
            summary = "Update message in global channel by Id",
            parameters = {@Parameter(name = "id", description = "Message Id")}
    )
    @PutMapping("/{id}")
    @VerifyJWT
    public MessageResponseDTO updateMessage(
            @RequestBody MessageRequestDTO messageRequestDTO,
            @PathVariable int id
    ){
        final var updatedMessage = messageService.updateMessageById(id, messageRequestDTO);

        return createMessageResponseDTO(updatedMessage);
    }


    @Operation(
            summary = "Delete message in global channel by Id",
            parameters = {@Parameter(name = "id", description = "Message Id")}
    )
    @DeleteMapping("/{id}")
    @VerifyJWT
    public void deleteMessage(
            @RequestBody MessageRequestDTO messageRequestDTO,
            @PathVariable int id
    ){
        messageService.updateMessageById(id, messageRequestDTO, true);
    }

    //region DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageRequestDTO implements UserNickname {
        private String nickname;
        private String content;
    }

    @Data
    @Builder
    public static class MessageResponseDTO {
        private int id;
        private String authorNick;
        private String content;
        private LocalDateTime createdAt;
    }
    //endregion

    private MessageResponseDTO createMessageResponseDTO(Message message){
        return MessageResponseDTO.builder()
                .id(message.getId())
                .authorNick(message.getAuthor().getNickname())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

}

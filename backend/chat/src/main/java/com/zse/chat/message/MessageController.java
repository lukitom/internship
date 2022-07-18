package com.zse.chat.message;

import com.zse.chat.login.VerifyJWT;
import com.zse.chat.user.User;
import com.zse.chat.user.UserNickname;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Messages")
@RequestMapping("/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Operation(summary = "Get all messages")
    @GetMapping
    public List<MessageResponseDTO> getMessages(){
        return messageService.getAllMessages().stream()
                .map(this::createMessageResponseDTO)
                .toList();
    }

    @Operation(
            summary = "Get message by Id",
            parameters = {@Parameter(name = "id", description = "Message Id")}
    )
    @GetMapping("/{id}")
    public MessageResponseDTO getMessageById(@PathVariable int id){
        Message message = messageService.getMessageById(id);

        return createMessageResponseDTO(message);
    }

    @Operation(summary = "Create new message")
    @PostMapping
    @VerifyJWT
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO messageRequestDTO){
        User author = userService.getUserByNick(messageRequestDTO.getNickname());
        Message savedMessage = messageService.saveMessage(messageRequestDTO, author);

        return  createMessageResponseDTO(savedMessage);
    }

    @Operation(
            summary = "Update message by Id",
            parameters = {@Parameter(name = "id", description = "Message Id")}
    )
    @PutMapping("/{id}")
    @VerifyJWT
    public MessageResponseDTO updateMessage(@RequestBody MessageRequestDTO messageRequestDTO, @PathVariable int id){
        Message updatedMessage = messageService.updateMessageById(id, messageRequestDTO);

        return createMessageResponseDTO(updatedMessage);
    }

    //region DTOs
    @Data
    @Builder
    static class MessageRequestDTO implements UserNickname {
        private String nickname;
        private String content;
    }

    @Data
    @Builder
    static class MessageResponseDTO {
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

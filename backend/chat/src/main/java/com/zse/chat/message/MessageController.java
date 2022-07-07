package com.zse.chat.message;

import com.zse.chat.user.User;
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

@Tag(name = "Messeges")
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
                .map(message -> MessageResponseDTO.builder()
                        .id(message.getId())
                        .authorNick(message.getAuthor().getNick())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }

    @Operation(
            summary = "Get message by Id",
            parameters = {
                    @Parameter(name = "id", description = "Message Id")
            })
    @GetMapping("/{id}")
    public MessageResponseDTO getMessageById(@PathVariable int id){
        Message message = messageService.getMessageById(id);

        return MessageResponseDTO.builder()
                .id(message.getId())
                .authorNick(message.getAuthor().getNick())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Operation(summary = "Create new message")
    @PostMapping
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO messageRequestDTO){
        User author = userService.getUserById(messageRequestDTO.getAuthorNick());
        Message savedMessage = messageService.sendMessage(messageRequestDTO, author);

        return MessageResponseDTO.builder()
                .id(savedMessage.getId())
                .authorNick(savedMessage.getAuthor().getNick())
                .content(savedMessage.getContent())
                .createdAt(savedMessage.getCreatedAt())
                .build();
    }

    @Operation(
            summary = "Update message with Id",
            parameters = {
                    @Parameter(name = "id", description = "Message Id")
            })
    @PutMapping("/{id}")
    public MessageResponseDTO updateMessage(@PathVariable int id, @RequestBody MessageRequestDTO messageRequestDTO){
        Message updatedMessage = messageService.updateMessageById(id, messageRequestDTO);

        return MessageResponseDTO.builder()
                .id(updatedMessage.getId())
                .authorNick(updatedMessage.getAuthor().getNick())
                .content(updatedMessage.getContent())
                .createdAt(updatedMessage.getCreatedAt())
                .build();
    }

    @Data
    @Builder
    static class MessageRequestDTO {
        private String authorNick;
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

}

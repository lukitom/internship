package com.zse.chat.message;

import com.zse.chat.user.User;
import com.zse.chat.user.UserService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public List<MessageResponseDTO> getMessages(){
        return messageService.getAllMessages().stream()
                .map(this::createMessageResponseDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public MessageResponseDTO getMessageById(@PathVariable int id){
        Message message = messageService.getMessageById(id);

        return createMessageResponseDTO(message);
    }

    @PostMapping
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO messageRequestDTO){
        User author = userService.getUserById(messageRequestDTO.getAuthorNick());
        Message savedMessage = messageService.sendMessage(messageRequestDTO, author);

        return  createMessageResponseDTO(savedMessage);
    }

    @PutMapping("/{id}")
    public MessageResponseDTO updateMessage(@PathVariable int id, @RequestBody MessageRequestDTO messageRequestDTO){
        Message updatedMessage = messageService.updateMessageById(id, messageRequestDTO);

        return createMessageResponseDTO(updatedMessage);
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

    private MessageResponseDTO createMessageResponseDTO(Message message){
        return MessageResponseDTO.builder()
                .id(message.getId())
                .authorNick(message.getAuthor().getNick())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

}

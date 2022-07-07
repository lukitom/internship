package com.zse.chat.message;

import com.zse.chat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAllByOrderByIdAsc();
    }

    public Message getMessageById(int id) {
        return messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
    }

    public Message sendMessage(MessageController.MessageRequestDTO messageRequestDTO, User user) {
        final Message newMessage = Message.builder()
                 .content(messageRequestDTO.getContent())
                 .author(user)
                 .createdAt(LocalDateTime.now())
                 .build();
         return messageRepository.save(newMessage);
    }

    public Message updateMessageById(int id, MessageController.MessageRequestDTO messageRequestDTO) {
        Message previousMessage = getMessageById(id);

        Message updatedMessage = Message.builder()
                .id(previousMessage.getId())
                .author(previousMessage.getAuthor())
                .content(messageRequestDTO.getContent())
                .createdAt(previousMessage.getCreatedAt())
                .build();

        return messageRepository.save(updatedMessage);
    }
}

package com.zse.chat.message;

import com.zse.chat.login.MessageUpdateFailedException;
import com.zse.chat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<Message> getAllMessagesInGlobalChannel() {
        return messageRepository.findAllByDeletedFalseAndChannelIsNullOrderByIdAsc();
    }

    public Message getMessageById(int id) {
        return messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
    }

    public Message saveMessage(MessageController.MessageRequestDTO messageRequestDTO, User user) {
        final var newMessage = Message.builder()
                 .content(messageRequestDTO.getContent())
                 .author(user)
                 .createdAt(LocalDateTime.now())
                 .build();
         return messageRepository.save(newMessage);
    }

    public Message updateMessageById(int id, MessageController.MessageRequestDTO messageRequestDTO){
        return updateMessageById(id, messageRequestDTO, false);
    }

    public Message updateMessageById(int id, MessageController.MessageRequestDTO messageRequestDTO, boolean delete) {
        final var previousMessage = getMessageById(id);

        if(previousMessage.isDeleted()){
            throw new MessageNotFoundException(previousMessage.getId());
        }

        if (!messageRequestDTO.getNickname().equals(previousMessage.getAuthor().getNickname())){
            throw new MessageUpdateFailedException();
        }

        final var updatedMessage = Message.builder()
                .id(previousMessage.getId())
                .author(previousMessage.getAuthor())
                .content(messageRequestDTO.getContent())
                .createdAt(previousMessage.getCreatedAt())
                .deleted(delete)
                .build();

        return messageRepository.save(updatedMessage);
    }

}

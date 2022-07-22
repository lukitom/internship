package com.zse.chat.message.channel;

import com.zse.chat.channel.Channel;
import com.zse.chat.login.MessageUpdateFailedException;
import com.zse.chat.message.Message;
import com.zse.chat.message.MessageController;
import com.zse.chat.message.MessageNotFoundException;
import com.zse.chat.message.MessageRepository;
import com.zse.chat.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageChannelService {

    private final MessageRepository messageRepository;

    public List<Message> getMessages(Channel channel) {
        return messageRepository.findAllByDeletedFalseAndChannelIsOrderByIdAsc(channel);
    }

    public Message getMessageById(int id, int channelId) {
        final var message = messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));

        final var channel = Optional.ofNullable(message.getChannel())
                .orElseThrow(() -> new MessageNotFoundException(id));

        if(channel.getId() != channelId){
            throw new MessageNotFoundException(id);
        }
        return message;
    }

    public Message saveMessage(
            MessageController.MessageRequestDTO messageRequestDTO,
            User user,
            Channel channel
    ) {
        final var newMessage = Message.builder()
                .content(messageRequestDTO.getContent())
                .author(user)
                .createdAt(LocalDateTime.now())
                .channel(channel).build();

        return messageRepository.save(newMessage);
    }

    public Message updateMessage(
            int messageId,
            MessageController.MessageRequestDTO messageRequestDTO,
            Channel channel
    ) {
        return updateMessage(messageId, messageRequestDTO, channel, false);
    }

    public Message updateMessage(
            int messageId,
            MessageController.MessageRequestDTO messageRequestDTO,
            Channel channel,
            boolean delete
    ) {
        final var previousMessage = getMessageById(messageId, channel.getId());

        if(previousMessage.isDeleted()){
            throw new MessageNotFoundException(previousMessage.getId());
        }

        if (!messageRequestDTO.getNickname().equals(previousMessage.getAuthor().getNickname())){
            throw new MessageUpdateFailedException();
        }

        final var updatedMessage = previousMessage.toBuilder()
                .content(messageRequestDTO.getContent())
                .deleted(delete)
                .build();

        return messageRepository.save(updatedMessage);
    }

}

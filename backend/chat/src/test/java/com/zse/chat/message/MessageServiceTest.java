package com.zse.chat.message;


import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MessageServiceTest {

    private MessageService messageService;

    @MockBean
    private MessageRepository messageRepository;

    //region fixture
    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository);
    }

    private MessageController.MessageRequestDTO.MessageRequestDTOBuilder createMessageRequest(
            int number, User user) {
        return MessageController.MessageRequestDTO.builder()
                .nickname(user.getNickname())
                .content("testContent" + number);
    }
    //endregion

    //region getAllMessages()
    @Test
    public void shouldReturnListOfMessages(){
        List<User> users = new ArrayList<>();
        users.add(UserFixture.createDefaultUser(1).build());
        users.add(UserFixture.createDefaultUser(2).build());
        users.add(UserFixture.createDefaultUser(3).build());

        List<Message> messages = new ArrayList<>();
        messages.add(MessageFixture.createDefaultMessage(1, users.get(0)).build());
        messages.add(MessageFixture.createDefaultMessage(2, users.get(1)).build());
        messages.add(MessageFixture.createDefaultMessage(3, users.get(2)).build());
        messages.add(MessageFixture.createDefaultMessage(4, users.get(0)).build());
        messages.add(MessageFixture.createDefaultMessage(5, users.get(2)).build());
        messages.add(MessageFixture.createDefaultMessage(6, users.get(2)).build());

        when(messageRepository.findAllByDeletedFalseAndChannelIsNullOrderByIdAsc()).thenReturn(messages);

        List<Message> returned = messageService.getAllMessagesInGlobalChannel();

        assertThat(returned, hasSize(6));
        verify(messageRepository, times(1)).findAllByDeletedFalseAndChannelIsNullOrderByIdAsc();
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldReturnEmptyListOfMessages(){
        List<Message> messages = new ArrayList<>();

        when(messageRepository.findAllByDeletedFalseAndChannelIsNullOrderByIdAsc()).thenReturn(messages);

        List<Message> returned = messageService.getAllMessagesInGlobalChannel();

        assertThat(returned, hasSize(0));
        verify(messageRepository, times(1)).findAllByDeletedFalseAndChannelIsNullOrderByIdAsc();
        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region getMessageById()
    @Test
    public void shouldReturnMessageById(){
        User user = UserFixture.createDefaultUser(1).build();
        Message message = MessageFixture.createDefaultMessage(1, user).build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));

        Message returned = messageService.getMessageById(1);

        assertThat(returned, notNullValue());
        assertThat(returned.getId(), equalTo(1));
        assertThat(returned.getAuthor(), equalTo(user));
        assertThat(returned.getContent(), equalTo("testContent1"));

        verify(messageRepository, times(1)).findById(anyInt());
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldThrowMessageNotFoundWhenTryingToFindByNotExistingId() {
        when(messageRepository.findById(1))
                .thenThrow(new MessageNotFoundException(1));

        assertThrows(MessageNotFoundException.class,
                () -> messageService.getMessageById(1));

        verify(messageRepository, times(1)).findById(anyInt());
        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region sendMessage()
    @Test
    public void shouldSaveMessage() {
        User user = UserFixture.createDefaultUser(1).build();
        var messageRequestDTO = createMessageRequest(1, user).build();

        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Message message = messageService.saveMessage(messageRequestDTO, user);

        assertThat(message, notNullValue());
        assertThat(message.getId(), notNullValue());
        assertThat(message.getAuthor(), equalToObject(user));
        assertThat(message.getContent(), equalTo("testContent1"));
        assertThat(message.getCreatedAt(), notNullValue());

        verify(messageRepository, times(1))
                .save(ArgumentMatchers.any(Message.class));
        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region updateMessageById()
    @Test
    public void shouldUpdateMessage() {
        User user = UserFixture.createDefaultUser(1).build();
        Message message = MessageFixture.createDefaultMessage(1, user).build();
        var messageRequestDTO = createMessageRequest(1, user).build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));
        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Message updatedMessage = messageService.updateMessageById(1, messageRequestDTO);

        assertThat(updatedMessage, notNullValue());
        assertThat(updatedMessage.getId(), notNullValue());
        assertThat(updatedMessage.getAuthor(), equalToObject(user));
        assertThat(updatedMessage.getContent(), equalTo("testContent1"));
        assertThat(updatedMessage.getCreatedAt(), notNullValue());

        verify(messageRepository, times(1)).findById(any());
        verify(messageRepository, times(1))
                .save(ArgumentMatchers.any(Message.class));
        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

}

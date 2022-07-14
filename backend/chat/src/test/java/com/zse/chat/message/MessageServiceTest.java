package com.zse.chat.message;


import com.zse.chat.user.User;
import com.zse.chat.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

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

    private User.UserBuilder createUser(int number){
        return User.builder()
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number)
                .phoneNumber("testPhoneNumber" + number)
                .country("testCountry" + number)
                .city("testCity" + number)
                .userLanguage(User.Language.POLISH)
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))
                .userStatus(UserStatus.OFFLINE)
                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .deleted(false);
    }

    private Message.MessageBuilder createMessage(int number, User user){
        return Message.builder()
                .id(1)
                .author(user)
                .content("testContent" + number)
                .createdAt(LocalDateTime.now());
    }

    private MessageController.MessageRequestDTO.MessageRequestDTOBuilder createMessageRequest(
            int number, User user) {
        return MessageController.MessageRequestDTO.builder()
                .authorNick(user.getNickname())
                .content("testContent" + number);
    }
    //endregion

    //region getAllMessages()
    @Test
    public void shouldReturnListOfMessages(){
        List<User> users = new ArrayList<>();
        users.add(createUser(1).build());
        users.add(createUser(2).build());
        users.add(createUser(3).build());

        List<Message> messages = new ArrayList<>();
        messages.add(createMessage(1, users.get(0)).build());
        messages.add(createMessage(2, users.get(1)).build());
        messages.add(createMessage(3, users.get(2)).build());
        messages.add(createMessage(4, users.get(0)).build());
        messages.add(createMessage(5, users.get(2)).build());
        messages.add(createMessage(6, users.get(2)).build());

        when(messageRepository.findAllByOrderByIdAsc()).thenReturn(messages);

        List<Message> returned = messageService.getAllMessages();

        assertThat(returned, hasSize(6));
        verify(messageRepository, times(1)).findAllByOrderByIdAsc();
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldReturnEmptyListOfMessages(){
        List<Message> messages = new ArrayList<>();

        when(messageRepository.findAllByOrderByIdAsc()).thenReturn(messages);

        List<Message> returned = messageService.getAllMessages();

        assertThat(returned, hasSize(0));
        verify(messageRepository, times(1)).findAllByOrderByIdAsc();
        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region getMessageById()
    @Test
    public void shouldReturnMessageById(){
        User user = createUser(1).build();
        Message message = createMessage(1, user).build();

        when(messageRepository.findById(1)).thenReturn(Optional.ofNullable(message));

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
    public void shouldSendMessage() {
        User user = createUser(1).build();
        var messageRequestDTO = createMessageRequest(1, user).build();

        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Message message = messageService.sendMessage(messageRequestDTO, user);

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
        User user = createUser(1).build();
        Message message = createMessage(1, user).build();
        var messageRequestDTO = createMessageRequest(1, user).build();

        when(messageRepository.findById(1)).thenReturn(Optional.ofNullable(message));
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
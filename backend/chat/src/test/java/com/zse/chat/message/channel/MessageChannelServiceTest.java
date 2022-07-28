package com.zse.chat.message.channel;

import com.zse.chat.channel.Channel;
import com.zse.chat.channel.ChannelFixture;
import com.zse.chat.login.MessageUpdateFailedException;
import com.zse.chat.message.*;
import com.zse.chat.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MessageChannelServiceTest {

    private MessageChannelService messageChannelService;

    @MockBean
    private MessageRepository messageRepository;

    //region fixture
    @Captor
    ArgumentCaptor<Message> captorMessage;

    @BeforeEach
    void setUp() {
        messageChannelService = new MessageChannelService(messageRepository);
    }
    //endregion

    //region getMessages()
    @Test
    public void shouldReturnListOfMessages () {
        final List<Message> messages = MessageFixture.createListOfMessages(10);
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(),
                List.of(),
                messages
        ).build();

        when(messageRepository.findAllByDeletedFalseAndChannelIsOrderByIdAsc(
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(messages);

        List<Message> returnedList = messageChannelService.getMessages(channel);

        assertEquals(returnedList, messages);
        assertThat(returnedList, hasSize(10));

        verify(messageRepository, times(1))
                .findAllByDeletedFalseAndChannelIsOrderByIdAsc(channel);

        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldReturnEmptyListOfMessages () {
        final List<Message> messages = new ArrayList<>();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(),
                List.of(),
                messages
        ).build();

        when(messageRepository.findAllByDeletedFalseAndChannelIsOrderByIdAsc(
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(messages);

        List<Message> returnedList = messageChannelService.getMessages(channel);

        assertEquals(returnedList, messages);
        assertThat(returnedList, empty());

        verify(messageRepository, times(1))
                .findAllByDeletedFalseAndChannelIsOrderByIdAsc(channel);

        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region getMessageById()
    @Test
    public void shouldReturnMessageFromChannelById () {
        final var user = UserFixture.createDefaultUser(1).build();
        var message = MessageFixture.createDefaultMessage(1, user).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of(message)
        ).build();
        message = message.toBuilder().channel(channel).build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));

        final var returnedMessage = messageChannelService.getMessageById(1, 1);

        assertEquals(returnedMessage, message);

        verify(messageRepository, times(1)).findById(1);

        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldThrowMessageNotFoundTryingToFindByNotExistingId () {
        final var user = UserFixture.createDefaultUser(1).build();
        final var message = MessageFixture.createDefaultMessage(1, user).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of(message)
        ).build();

        when(messageRepository.findById(2)).thenReturn(Optional.empty());

        final var result = assertThrows(MessageNotFoundException.class,
                () -> messageChannelService.getMessageById(2, 1));

        assertThat(result.getMessage(), containsString("2"));

        verify(messageRepository, times(1)).findById(2);

        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldThrowMessageNotFoundTryingToFindGetChannelWhereMessageShouldBe () {
        final var user = UserFixture.createDefaultUser(1).build();
        final var message = MessageFixture.createDefaultMessage(1, user).build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));

        final var result = assertThrows(MessageNotFoundException.class,
                () -> messageChannelService.getMessageById(1, 1));

        assertThat(result.getMessage(), containsString("1"));

        verify(messageRepository, times(1)).findById(1);

        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldThrowMessageNotFoundTryingGetMessageThatNotBelongToChannelWithId () {
        final var user = UserFixture.createDefaultUser(1).build();
        var message = MessageFixture.createDefaultMessage(1, user).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of(message)
        ).build();
        message = message.toBuilder().channel(channel).build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));

        final var result = assertThrows(MessageNotFoundException.class,
                () -> messageChannelService.getMessageById(1, 2));

        assertThat(result.getMessage(), containsString("1"));

        verify(messageRepository, times(1)).findById(1);

        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region saveMessage()
    @Test
    public void shouldReturnSavedMessageAndValidIt() {
        final var user = UserFixture.createDefaultUser(1).build();
        final List<Message> messages = new ArrayList<>();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .content("testContent1")
                .nickname("testNickname1")
                .build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                messages
        ).build();

        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Message savedMessage = messageChannelService.saveMessage(messageRequestDTO, user, channel);

        assertThat(savedMessage.getAuthor().getNickname(), equalTo("testNickname1"));
        assertThat(savedMessage.getContent(), equalTo("testContent1"));

        verify(messageRepository, times(1)).save(
                captorMessage.capture()
        );

        assertThat(captorMessage.getValue().getContent(), equalTo("testContent1"));
        assertThat(captorMessage.getValue().getAuthor(), equalTo(user));
        assertThat(captorMessage.getValue().getChannel(), equalTo(channel));

        verifyNoMoreInteractions(messageRepository);
    }
    //endregion

    //region updateMessage(4)
    @Test
    public void shouldReturnUpdatedAndValidMessage() {
        final var user = UserFixture.createDefaultUser(1).build();
        var message = MessageFixture.createDefaultMessage(1, user).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of(message)
        ).build();
        message = message.toBuilder().channel(channel).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname("testNickname1")
                .content("testContent1Updated")
                .build();
        final var updatedMessage = message.toBuilder()
                .content("testContent1Updated")
                .deleted(true)
                .build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));
        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var returnedMessage = messageChannelService.updateMessage(
                1,
                messageRequestDTO,
                channel,
                true
        );

        assertThat(returnedMessage.getChannel(), equalTo(channel));
        assertThat(returnedMessage.getContent(), equalTo(updatedMessage.getContent()));
        assertThat(returnedMessage.isDeleted(), equalTo(true));

        verify(messageRepository, times(1)).findById(1);
        verify(messageRepository, times(1)).save(returnedMessage);

        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldThrowMessageNotFoundTryingToUpdateDeletedMessage() {
        final var user = UserFixture.createDefaultUser(1).build();
        var message = MessageFixture.createDefaultMessage(1, user)
                .deleted(true)
                .build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of(message)
        ).build();
        message = message.toBuilder().channel(channel).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname("testNickname1")
                .content("testContent1Updated")
                .build();
        final var updatedMessage = message.toBuilder()
                .content("testContent1Updated")
                .deleted(true)
                .build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));
        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var result = assertThrows(
                MessageNotFoundException.class,
                () -> messageChannelService.updateMessage(
                        1,
                        messageRequestDTO,
                        channel,
                        true
                )
        );

        assertThat(result.getMessage(), containsString("1"));

        verify(messageRepository, times(1)).findById(1);

        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    public void shouldThrowMessageUpdateFailedTryingToUpdateNotOwningMessage() {
        final var user = UserFixture.createDefaultUser(1).build();
        var message = MessageFixture.createDefaultMessage(1, user).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of(message)
        ).build();
        message = message.toBuilder().channel(channel).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname("testNickname2")
                .content("testContent1Updated")
                .build();

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));
        when(messageRepository.save(ArgumentMatchers.any(Message.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var result = assertThrows(
                MessageUpdateFailedException.class,
                () -> messageChannelService.updateMessage(
                        1,
                        messageRequestDTO,
                        channel,
                        true
                )
        );

        assertThat(result.getMessage(), containsString("not possible"));

        verify(messageRepository, times(1)).findById(1);

        verifyNoMoreInteractions(messageRepository);
    }
    //endregion
}

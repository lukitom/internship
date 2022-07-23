package com.zse.chat.channel;

import com.zse.chat.message.MessageFixture;
import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.verification.VerificationMode;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ChannelServiceTest {
    
    private ChannelService channelService;
    
    @MockBean
    private ChannelRepository channelRepository;

    //region fixture
    @BeforeEach
    void setUp() {
        channelService = new ChannelService(channelRepository);
    }
    //endregion

    //region getChannels()
    @Test
    public void shouldReturnChannelsWhereUserIsOwnerOrMember() {
        List<Channel> channels = new ArrayList<>();
        User user = UserFixture.createDefaultUser(1).build();

        channels.add(
                ChannelFixture.createDefaultChannel(
                        1,
                        UserFixture.createListOfDefaultUser(3),
                        UserFixture.createListOfDefaultUser(6, 2),
                        MessageFixture.createListOfMessages(4)
                ).build()
        );
        channels.add(
                ChannelFixture.createDefaultChannel(
                        3,
                        UserFixture.createListOfDefaultUser(5, 1),
                        UserFixture.createListOfDefaultUser(1),
                        MessageFixture.createListOfMessages(3)
                ).build()
        );

        when(channelRepository.getChannelsByOwnersInOrMembersIn(
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList()
        )).thenReturn(channels);

        List<Channel> returnedChannels = channelService.getChannels(user);

        assertEquals(returnedChannels, channels);

        verify(channelRepository, times(1)).getChannelsByOwnersInOrMembersIn(
                List.of(user),
                List.of(user)
        );
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldReturnEmptyArrayOfChannelsWhereUserIsOwnerOrMember() {
        User user = UserFixture.createDefaultUser(1).build();
        List<Channel> channels = new ArrayList<>();

        when(channelRepository.getChannelsByOwnersInOrMembersIn(
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList()
        )).thenReturn(channels);

        List<Channel> returnedChannels = channelService.getChannels(user);

        assertThat(returnedChannels, is(empty()));

        verify(channelRepository, times(1)).getChannelsByOwnersInOrMembersIn(
                List.of(user),
                List.of(user)
        );
        verifyNoMoreInteractions(channelRepository);
    }
    //endregion

    //region getChannelById()
    @Test
    public void shouldReturnChannelById() {
        Channel channel = ChannelFixture.createDefaultChannel(
                1,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        ).build();

        when(channelRepository.findById(1)).thenReturn(Optional.of(channel));

        Channel returnedChannel = channelService.getChannelById(1);

        assertEquals(returnedChannel, channel);

        verify(channelRepository, times(1)).findById(1);
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldThrowChannelNotFoundWhenTryingToFindByNotExistingId() {
        when(channelRepository.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                ChannelNotFoundException.class,
                () -> channelService.getChannelById(0));

        verify(channelRepository, times(1)).findById(0);
        verifyNoMoreInteractions(channelRepository);
    }
    //endregion

    //region saveChannel()

    //endregion

    //region userHasPermissionToUpdateChannel()

    //endregion

    //region updateChannel

    //endregion

    //region userHasPermissionToSeeChannel

    //endregion

}

package com.zse.chat.channel;

import com.zse.chat.message.MessageFixture;
import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import org.assertj.core.util.Sets;
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
        final List<Channel> channels = new ArrayList<>();
        final var user = UserFixture.createDefaultUser(1).build();

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

        final List<Channel> returnedChannels = channelService.getChannels(user);

        assertEquals(returnedChannels, channels);

        verify(channelRepository, times(1)).getChannelsByOwnersInOrMembersIn(
                List.of(user),
                List.of(user)
        );
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldReturnEmptyArrayOfChannelsWhereUserIsOwnerOrMember() {
        final var user = UserFixture.createDefaultUser(1).build();
        final List<Channel> channels = new ArrayList<>();

        when(channelRepository.getChannelsByOwnersInOrMembersIn(
                ArgumentMatchers.anyList(),
                ArgumentMatchers.anyList()
        )).thenReturn(channels);

        final List<Channel> returnedChannels = channelService.getChannels(user);

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

        final var returnedChannel = channelService.getChannelById(1);

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
    @Test
    public void shouldSaveChannel() {
        final var user = UserFixture.createDefaultUser(1).build();

        when(channelRepository.save(ArgumentMatchers.any(Channel.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var channel = channelService.saveChannel(user);

        assertEquals(channel.getOwners(), List.of(user));
        assertEquals(channel.getMembers(), List.of());

        verify(channelRepository, times(1)).save(ArgumentMatchers.any(Channel.class));
        verifyNoMoreInteractions(channelRepository);
    }
    //endregion

    //region userHasPermissionToUpdateChannel()
    @Test
    public void shouldReturnTrueWhenUserCanUpdateChannel() {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of()
        ).build();

        boolean result = channelService.userHasPermissionToUpdateChannel(channel, user.getNickname());

        assertThat(result, is(true));

        verifyNoInteractions(channelRepository);
    }

    @Test
    public void shouldReturnFalseWhenUserCanNotUpdateChannel() {
        final var user1 = UserFixture.createDefaultUser(1).build();
        final var user2 = UserFixture.createDefaultUser(2).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user1),
                List.of(),
                List.of()
        ).build();

        boolean result = channelService.userHasPermissionToUpdateChannel(channel, user2.getNickname());

        assertThat(result, is(false));

        verifyNoInteractions(channelRepository);
    }
    //endregion

    //region updateChannel
    @Test
    public void shouldAddOwnerUpdateChannel() {
        final var action = ChannelUpdateAction.ADD_OWNER;
        final var userThatSendRequest = UserFixture.createDefaultUser(1).build();
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = new ArrayList<>();
        owners.add(userThatSendRequest);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                new ArrayList<>(),
                List.of()
        ).build();

        when(channelRepository.save(ArgumentMatchers.any(Channel.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var updatedChannel = channelService.updateChannel(channel, action, userToManipulate);

        assertEquals(updatedChannel.getOwners().size(), 2);
        assertEquals(updatedChannel.getOwners().get(0).getNickname(), "testNickname1");
        assertEquals(updatedChannel.getOwners().get(1).getNickname(), "testNickname2");

        assertEquals(updatedChannel.getMembers().size(), 0);

        verify(channelRepository, times(1)).save(ArgumentMatchers.any(Channel.class));
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldRemoveOwnerUpdateChannel() {
        final var action = ChannelUpdateAction.REMOVE_OWNER;
        final var userThatSendRequest = UserFixture.createDefaultUser(1).build();
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = new ArrayList<>();
        owners.add(userThatSendRequest);
        owners.add(userToManipulate);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                new ArrayList<>(),
                List.of()
        ).build();

        when(channelRepository.save(ArgumentMatchers.any(Channel.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var updatedChannel = channelService.updateChannel(channel, action, userToManipulate);

        assertEquals(updatedChannel.getOwners().size(), 1);
        assertEquals(updatedChannel.getOwners().get(0).getNickname(), "testNickname1");
        assertEquals(updatedChannel.getMembers().size(), 1);
        assertEquals(updatedChannel.getMembers().get(0).getNickname(), "testNickname2");

        verify(channelRepository, times(1)).save(ArgumentMatchers.any(Channel.class));
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldAddMemberFirstTimeUpdateChannel() {
        final var action = ChannelUpdateAction.ADD_MEMBER;
        final var userThatSendRequest = UserFixture.createDefaultUser(1).build();
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = new ArrayList<>();
        owners.add(userThatSendRequest);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                new ArrayList<>(),
                List.of()
        ).build();

        when(channelRepository.save(ArgumentMatchers.any(Channel.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var updatedChannel = channelService.updateChannel(channel, action, userToManipulate);

        assertEquals(updatedChannel.getOwners().size(), 1);
        assertEquals(updatedChannel.getOwners().get(0).getNickname(), "testNickname1");
        assertEquals(updatedChannel.getMembers().size(), 1);
        assertEquals(updatedChannel.getMembers().get(0).getNickname(), "testNickname2");

        verify(channelRepository, times(1)).save(ArgumentMatchers.any(Channel.class));
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldNotAddMemberBecauseIsAlreadyMember() {
        final var action = ChannelUpdateAction.ADD_MEMBER;
        final var userThatSendRequest = UserFixture.createDefaultUser(1).build();
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = new ArrayList<>();
        owners.add(userThatSendRequest);

        final List<User> members = new ArrayList<>();
        members.add(userToManipulate);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                members,
                List.of()
        ).build();

        when(channelRepository.save(ArgumentMatchers.any(Channel.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var updatedChannel = channelService.updateChannel(channel, action, userToManipulate);

        final var membersAfterSet = Sets.newHashSet(updatedChannel.getMembers());
        final var membersAfter = new ArrayList<>(membersAfterSet);
        assertEquals(updatedChannel.getOwners().size(), 1);
        assertEquals(updatedChannel.getOwners().get(0).getNickname(), "testNickname1");
        assertEquals(membersAfter.size(), 1);
        assertEquals(membersAfter.get(0).getNickname(), "testNickname2");

        verify(channelRepository, times(1)).save(ArgumentMatchers.any(Channel.class));
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    public void shouldRemoveMemberFirstTimeUpdateChannel() {
        final var action = ChannelUpdateAction.REMOVE_MEMBER;
        final var userThatSendRequest = UserFixture.createDefaultUser(1).build();
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = new ArrayList<>();
        owners.add(userThatSendRequest);
        final List<User> members = new ArrayList<>();
        members.add(userToManipulate);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                members,
                List.of()
        ).build();

        when(channelRepository.save(ArgumentMatchers.any(Channel.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        final var updatedChannel = channelService.updateChannel(channel, action, userToManipulate);

        assertEquals(updatedChannel.getOwners().size(), 1);
        assertEquals(updatedChannel.getOwners().get(0).getNickname(), "testNickname1");
        assertEquals(updatedChannel.getMembers().size(), 0);

        verify(channelRepository, times(1)).save(ArgumentMatchers.any(Channel.class));
        verifyNoMoreInteractions(channelRepository);
    }
    //endregion

    //region userHasPermissionToSeeChannel
    @Test
    public void shouldReturnTrueWhenUserIsOwner() {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of()
        ).build();

        boolean result = channelService.userHasPermissionToSeeChannel(channel, user.getNickname());

        assertThat(result, is(true));
        verifyNoInteractions(channelRepository);
    }

    @Test
    public void shouldReturnTrueWhenUserIsMember() {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(),
                List.of(user),
                List.of()
        ).build();

        boolean result = channelService.userHasPermissionToSeeChannel(channel, user.getNickname());

        assertThat(result, is(true));
        verifyNoInteractions(channelRepository);
    }

    @Test
    public void shouldReturnFalseWhenUserIsNonOwnerOrNotMember() {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(),
                List.of(),
                List.of()
        ).build();

        boolean result = channelService.userHasPermissionToSeeChannel(channel, user.getNickname());

        assertThat(result, is(false));
        verifyNoInteractions(channelRepository);
    }
    //endregion

}

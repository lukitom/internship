package com.zse.chat.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zse.chat.message.MessageFixture;
import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import com.zse.chat.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ChannelController channelController;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private UserService userService;

    //region fixture

    //endregion

    //region GET("/channels")
    @Test
    public void shouldReturnAllMessagesForOwnerInChannel() {
        User user = UserFixture.createDefaultUser(1).build();

        List<User> owners = UserFixture.createListOfDefaultUser(10);
        List<User> members = UserFixture.createListOfDefaultUser(20, 20);

        List<Channel> channels = new ArrayList<>();
        channels.add(
                ChannelFixture.createDefaultChannel(
                        1,
                        owners,
                        members,
                        MessageFixture.createListOfMessages(5)
                ).build()
        );

        when(userService.getUserByNick("testNick"))
                .thenReturn(user);
        when(channelService.getChannels(user))
                .thenReturn(channels);
    }
    //endregion

    //region POST("/channels")

    //endregion

    //region PUT("/channels/users")

    //endregion

}
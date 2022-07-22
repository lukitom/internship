package com.zse.chat.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import com.zse.chat.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

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
    public void shouldReturnAllMessagesForOwnerInChannel() throws Exception {
        User user = UserFixture.createDefaultUser(1).build();

        List<Channel> channels = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            channels.add(
                    ChannelFixture.createDefaultChannel(
                            i + 1,
                            null,
                            null,
                            null
                    ).build()
            );
        }

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.getChannels(ArgumentMatchers.any(User.class)))
                .thenReturn(channels);

        mockMvc.perform(get("/channels"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

    }
    //endregion

    //region POST("/channels")

    //endregion

    //region PUT("/channels/users")

    //endregion

}
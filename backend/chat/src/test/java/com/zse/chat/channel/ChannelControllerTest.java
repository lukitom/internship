package com.zse.chat.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zse.chat.login.LoginController;
import com.zse.chat.login.VerifyUser;
import com.zse.chat.message.MessageFixture;
import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import com.zse.chat.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ChannelController.class, LoginController.class})
@Import({AopAutoConfiguration.class, VerifyUser.class})
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
    private String tokenJWT;

    @BeforeEach
    void setUp() throws Exception {
        if (tokenJWT != null){
            return;
        }

        final var token =mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"testNickname1\"}"))
                .andReturn();

        tokenJWT = token.getResponse().getContentAsString();
    }

    private MockHttpServletRequestBuilder authorize(MockHttpServletRequestBuilder pathBuilder){
        return pathBuilder.header("Authorization", "Bearer " + tokenJWT);
    }
    //endregion

    //region GET("/channels")
    @Test
    public void shouldReturnAllAvailableToSeeChannelsForUser() throws Exception {
        User user = UserFixture.createDefaultUser(1).build();

        List<Channel> channels = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            channels.add(
                    ChannelFixture.createDefaultChannel(
                            i + 1,
                            UserFixture.createListOfDefaultUser(1),
                            UserFixture.createListOfDefaultUser(2, 1),
                            null
                    ).build()
            );
        }

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.getChannels(ArgumentMatchers.any(User.class)))
                .thenReturn(channels);

        mockMvc.perform(authorize(get("/channels")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    public void shouldReturnEmptyArrayOfAvailableChannelsForUser() throws Exception {
        User user = UserFixture.createDefaultUser(1).build();
        List<Channel> channels = new ArrayList<>();

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.getChannels(ArgumentMatchers.any(User.class))).thenReturn(channels);

        mockMvc.perform(authorize(get("/channels")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    //endregion

    //region POST("/channels")
    @Test
    public void shouldCreateChannel() throws Exception {
        User user = UserFixture.createDefaultUser(1).build();
        Channel channel = ChannelFixture.createDefaultChannel(
                1,
                UserFixture.createListOfDefaultUser(1),
                UserFixture.createListOfDefaultUser(2, 1),
                MessageFixture.createListOfMessages(5)
        ).build();

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.saveChannel(user)).thenReturn(channel);

        mockMvc.perform(authorize(post("/channels")))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldValidateCreateChannelResponse() throws Exception {
        User user = UserFixture.createDefaultUser(1).build();
        Channel channel = ChannelFixture.createDefaultChannel(
                1,
                UserFixture.createListOfDefaultUser(1),
                UserFixture.createListOfDefaultUser(2, 1),
                MessageFixture.createListOfMessages(5)
        ).build();

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.saveChannel(user)).thenReturn(channel);

        mockMvc.perform(authorize(post("/channels")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0]", equalTo("testNickname2")));
    }

    //endregion

    //region PUT("/channels/users")

    //endregion

}
package com.zse.chat.message.channel;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zse.chat.channel.Channel;
import com.zse.chat.channel.ChannelFixture;
import com.zse.chat.channel.ChannelService;
import com.zse.chat.login.LoginController;
import com.zse.chat.login.VerifyUser;
import com.zse.chat.message.MessageController;
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
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({MessageInChannelController.class, LoginController.class})
@Import({AopAutoConfiguration.class, VerifyUser.class})
class MessageInChannelControllerTest {

    //region fixture
    private static String tokenJWT;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Environment env;
    @MockBean
    private MessageChannelService messageChannelService;
    @MockBean
    private UserService userService;
    @MockBean
    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        final var secret = env.getProperty("jwt.secret");
        tokenJWT = JWT.create()
                .withClaim("nickname", "testNickname1")
                .withExpiresAt(Date.valueOf(LocalDate.now().plusDays(7)))
                .sign(Algorithm.HMAC256(secret));
    }

    private HttpHeaders authorize() {
        final var header = new HttpHeaders();
        header.setBearerAuth(tokenJWT);
        return header;
    }
    //endregion

    //region GET("/messages/channels/{channelId}")
    @Test
    public void shouldReturnAllMessagesFromChannel() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var messages = MessageFixture.createListOfMessages(10);
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                messages
        ).build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(channelService.userHasPermissionToSeeChannel(channel, "testNickname1"))
                .thenReturn(true);

        mockMvc.perform(get("/messages/channels/1")
                        .headers(authorize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    public void shouldReturnEmptyArrayOfMessagesFromChannel() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                List.of()
        ).build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(channelService.userHasPermissionToSeeChannel(channel, "testNickname1"))
                .thenReturn(true);

        mockMvc.perform(get("/messages/channels/1")
                        .headers(authorize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldThrowChannelAccessFailedExceptionForUserThatNotIsMemberOrOwnerInChannel() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var user2 = UserFixture.createDefaultUser(2).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user2),
                List.of(),
                List.of()
        ).build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(channelService.userHasPermissionToSeeChannel(channel, user.getNickname()))
                .thenReturn(false);

        mockMvc.perform(get("/messages/channels/1")
                        .headers(authorize()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.responseCode", equalTo(403)))
                .andExpect(jsonPath("$.exceptionMessage", containsString("not possible")));
    }
    //endregion

    //region POST("/messages/channels/{channelId}")
    @Test
    public void shouldReturnCreatedMessage() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                new ArrayList<>()
        ).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .content("testContent1").build();
        final var body = mapper.writeValueAsString(messageRequestDTO);
        final var message = MessageFixture.createDefaultMessage(1, user).build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(userService.getUserByNick(user.getNickname())).thenReturn(user);
        when(messageChannelService.saveMessage(
                ArgumentMatchers.any(MessageController.MessageRequestDTO.class),
                ArgumentMatchers.any(User.class),
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(message);
        when(channelService.userHasPermissionToSeeChannel(channel, "testNickname1"))
                .thenReturn(true);

        mockMvc.perform(post("/messages/channels/1")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void verifyCorrectnessOfCreatedMessage() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                new ArrayList<>()
        ).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .content("testContent1").build();
        final var body = mapper.writeValueAsString(messageRequestDTO);
        final var message = MessageFixture.createDefaultMessage(1, user).build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(userService.getUserByNick(user.getNickname())).thenReturn(user);
        when(messageChannelService.saveMessage(
                ArgumentMatchers.any(MessageController.MessageRequestDTO.class),
                ArgumentMatchers.any(User.class),
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(message);
        when(channelService.userHasPermissionToSeeChannel(channel, "testNickname1"))
                .thenReturn(true);

        mockMvc.perform(post("/messages/channels/1")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.authorNick", equalTo("testNickname1")))
                .andExpect(jsonPath("$.content", equalTo("testContent1")));
    }
    //endregion

    //region PUT("/messages/channels/{channelId}/{messageId}")
    @Test
    public void shouldReturnUpdatedMessage() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var messages = MessageFixture.createListOfMessages(1);
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                messages
        ).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .content("testContentUpdated1").build();
        final var body = mapper.writeValueAsString(messageRequestDTO);
        final var updatedMessage = messages.get(0).toBuilder().content("testContentUpdated1)").build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(channelService.userHasPermissionToSeeChannel(channel, user.getNickname())).thenReturn(true);
        when(messageChannelService.updateMessage(
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(MessageController.MessageRequestDTO.class),
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(updatedMessage);

        mockMvc.perform(put("/messages/channels/1/1")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void validateUpdatedMessage() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var messages = MessageFixture.createListOfMessages(1);
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                messages
        ).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .content("testContentUpdated1").build();
        final var body = mapper.writeValueAsString(messageRequestDTO);
        final var updatedMessage = messages.get(0).toBuilder().content("testContentUpdated1").build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(channelService.userHasPermissionToSeeChannel(channel, user.getNickname())).thenReturn(true);
        when(messageChannelService.updateMessage(
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(MessageController.MessageRequestDTO.class),
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(updatedMessage);

        mockMvc.perform(put("/messages/channels/1/1")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.authorNick", equalTo("testNickname1")))
                .andExpect(jsonPath("$.content", equalTo("testContentUpdated1")));
    }
    //endregion

    //region DELETE("/messages/channels/{channelId}/{messageId}")
    @Test
    public void validateThatMessageHasBeenDeleted() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var messages = MessageFixture.createListOfMessages(1);
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                List.of(user),
                List.of(),
                messages
        ).build();
        final var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .content("testContentUpdated1").build();
        final var body = mapper.writeValueAsString(messageRequestDTO);
        final var updatedMessage = messages.get(0).toBuilder().content("testContentUpdated1").build();

        when(channelService.getChannelById(1)).thenReturn(channel);
        when(channelService.userHasPermissionToSeeChannel(channel, user.getNickname())).thenReturn(true);
        when(messageChannelService.updateMessage(
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(MessageController.MessageRequestDTO.class),
                ArgumentMatchers.any(Channel.class)
        )).thenReturn(updatedMessage);

        mockMvc.perform(put("/messages/channels/1/1")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }
    //endregion

}
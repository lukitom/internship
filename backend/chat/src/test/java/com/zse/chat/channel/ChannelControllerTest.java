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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private ChannelService channelService;

    @MockBean
    private UserService userService;

    //region fixture
    private String tokenJWT;

    public static Stream<MockHttpServletRequestBuilder> paths() {
        return Stream.of(
                get("/channels"),
                post("/channels"),
                put("/channels/users")
        );
    }

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
    @Test
    public void shouldAddChannelOwner() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        List<User> ownersBefore = UserFixture.createListOfDefaultUser(1);
        List<User> ownersAfter = UserFixture.createListOfDefaultUser(2);
        List<User> membersBefore = UserFixture.createListOfDefaultUser(2, 1);
        List<User> membersAfter = new ArrayList<>();

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                ownersBefore,
                membersBefore,
                new ArrayList<>()
        ).build();
        final var action = ChannelUpdateAction.ADD_OWNER;

        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .action(action).build();

        final var body = mapper.writeValueAsString(channelRequestDTO);

        when(channelService.getChannelById(ArgumentMatchers.any(Integer.class)))
                .thenReturn(channel);
        when(channelService.userHasPermissionToUpdateChannel(
                ArgumentMatchers.any(Channel.class), eq("testNickname1")))
                .thenReturn(true);
        when(userService.getUserByNick("testNickname2")).thenReturn(userToManipulate);
        when(channelService.updateChannel(channel, action, userToManipulate))
                .thenReturn(channel.toBuilder()
                            .owners(ownersAfter)
                            .members(membersAfter)
                            .build());

        mockMvc.perform(authorize(put("/channels/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(2)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.owners[1]", equalTo("testNickname2")))
                .andExpect(jsonPath("$.members", hasSize(0)));
    }

    @Test
    public void shouldRemoveChannelOwner() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        List<User> ownersBefore = UserFixture.createListOfDefaultUser(2);
        List<User> ownersAfter = UserFixture.createListOfDefaultUser(1);
        List<User> membersBefore = new ArrayList<>();
        List<User> membersAfter = UserFixture.createListOfDefaultUser(2,1);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                ownersBefore,
                membersBefore,
                new ArrayList<>()
        ).build();

        final var action = ChannelUpdateAction.REMOVE_OWNER;

        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .action(action).build();

        final var body = mapper.writeValueAsString(channelRequestDTO);

        when(channelService.getChannelById(ArgumentMatchers.any(Integer.class)))
                .thenReturn(channel);
        when(channelService.userHasPermissionToUpdateChannel(
                ArgumentMatchers.any(Channel.class), eq("testNickname1")))
                .thenReturn(true);
        when(userService.getUserByNick("testNickname2")).thenReturn(userToManipulate);
        when(channelService.updateChannel(channel, action, userToManipulate))
                .thenReturn(channel.toBuilder()
                        .owners(ownersAfter)
                        .members(membersAfter)
                        .build());

        mockMvc.perform(authorize(put("/channels/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0]", equalTo("testNickname2")));
    }

    @Test
    public void shouldAddChannelMember() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        List<User> owners = UserFixture.createListOfDefaultUser(1);
        List<User> membersBefore = new ArrayList<>();
        List<User> membersAfter = UserFixture.createListOfDefaultUser(2,1);

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                membersBefore,
                new ArrayList<>()
        ).build();

        final var action = ChannelUpdateAction.ADD_MEMBER;

        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .action(action).build();

        final var body = mapper.writeValueAsString(channelRequestDTO);

        when(channelService.getChannelById(ArgumentMatchers.any(Integer.class)))
                .thenReturn(channel);
        when(channelService.userHasPermissionToUpdateChannel(
                ArgumentMatchers.any(Channel.class), eq("testNickname1")))
                .thenReturn(true);
        when(userService.getUserByNick("testNickname2")).thenReturn(userToManipulate);
        when(channelService.updateChannel(channel, action, userToManipulate))
                .thenReturn(channel.toBuilder()
                        .owners(owners)
                        .members(membersAfter)
                        .build());

        mockMvc.perform(authorize(put("/channels/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0]", equalTo("testNickname2")));
    }

    @Test
    public void shouldRemoveChannelMember() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        List<User> owners = UserFixture.createListOfDefaultUser(1);
        List<User> membersBefore = UserFixture.createListOfDefaultUser(2,1);
        List<User> membersAfter = new ArrayList<>();

        final var channel = ChannelFixture.createDefaultChannel(
                1,
                owners,
                membersBefore,
                new ArrayList<>()
        ).build();

        final var action = ChannelUpdateAction.REMOVE_MEMBER;

        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .action(action).build();

        final var body = mapper.writeValueAsString(channelRequestDTO);

        when(channelService.getChannelById(ArgumentMatchers.any(Integer.class)))
                .thenReturn(channel);
        when(channelService.userHasPermissionToUpdateChannel(
                ArgumentMatchers.any(Channel.class), eq("testNickname1")))
                .thenReturn(true);
        when(userService.getUserByNick("testNickname2")).thenReturn(userToManipulate);
        when(channelService.updateChannel(channel, action, userToManipulate))
                .thenReturn(channel.toBuilder()
                        .owners(owners)
                        .members(membersAfter)
                        .build());

        mockMvc.perform(authorize(put("/channels/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(0)));
    }
    //TODO: check the rest of cases

    @ParameterizedTest
    @EnumSource(ChannelUpdateAction.class)
    public void shouldThrowChannelUpdateFailedExceptionUserWithoutPermission(
            ChannelUpdateAction action
    ) throws Exception {
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        ).build();

        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .action(action)
                .build();

        final var body = mapper.writeValueAsString(channelRequestDTO);

        when(channelService.getChannelById(ArgumentMatchers.any(Integer.class)))
                .thenReturn(channel);
        when(channelService.userHasPermissionToUpdateChannel(
                ArgumentMatchers.any(Channel.class), eq("testNickname1")))
                .thenReturn(false);

        mockMvc.perform(authorize(put("/channels/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.responseCode", equalTo(403)))
                .andExpect(jsonPath("$.exceptionMessage",
                        containsString("not possible")));
    }
    //endregion

    //region verification JWT
    @ParameterizedTest
    @MethodSource("paths")
    public void shouldThrowMissingAuthenticationToken(
            MockHttpServletRequestBuilder path
    ) throws Exception {
        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .build();

        mockMvc.perform(path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(channelRequestDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.responseCode", equalTo(401)))
                .andExpect(jsonPath("$.exceptionMessage",
                        equalTo("Missing authentication token")));
    }

    @ParameterizedTest
    @MethodSource("paths")
    public void shouldThrowInvalidJWTException(
            MockHttpServletRequestBuilder path
    ) throws Exception {
        final var channelRequestDTO = ChannelController.ChannelRequestDTO.builder()
                .id(1)
                .userNickname("testNickname2")
                .build();

        mockMvc.perform(path
                        .header("Authorization", "Bearer " + tokenJWT + "x")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(channelRequestDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.responseCode", equalTo(401)))
                .andExpect(jsonPath("$.exceptionMessage",
                        equalTo("Provided authentication token is invalid")));
    }
    //endregion

}

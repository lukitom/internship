package com.zse.chat.channel;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    @Autowired
    private Environment env;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private UserService userService;

    //region fixture
    private static String tokenJWT;

    public static Stream<MockHttpServletRequestBuilder> paths() {
        return Stream.of(
                get("/channels"),
                post("/channels"),
                put("/channels/users")
        );
    }

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

    //region GET("/channels")
    @Test
    public void shouldReturnChannels() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();

        final List<Channel> channels = new ArrayList<>();

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

        mockMvc.perform(get("/channels").headers(authorize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        verify(userService, times(1)).getUserByNick("testNickname1");
        verify(channelService, times(1)).getChannels(user);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturnEmptyArrayOfAvailableChannelsForUser() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.getChannels(ArgumentMatchers.any(User.class))).thenReturn(List.of());

        mockMvc.perform(get("/channels").headers(authorize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getUserByNick("testNickname1");
        verify(channelService, times(1)).getChannels(user);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }
    //endregion

    //region POST("/channels")
    @Test
    public void shouldCreateChannel() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                UserFixture.createListOfDefaultUser(1),
                UserFixture.createListOfDefaultUser(2, 1),
                MessageFixture.createListOfMessages(5)
        ).build();

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.saveChannel(user)).thenReturn(channel);

        mockMvc.perform(post("/channels").headers(authorize()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserByNick("testNickname1");
        verify(channelService, times(1)).saveChannel(user);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldValidateCreateChannelResponse() throws Exception {
        final var user = UserFixture.createDefaultUser(1).build();
        final var channel = ChannelFixture.createDefaultChannel(
                1,
                UserFixture.createListOfDefaultUser(1),
                UserFixture.createListOfDefaultUser(2, 1),
                MessageFixture.createListOfMessages(5)
        ).build();

        when(userService.getUserByNick("testNickname1")).thenReturn(user);
        when(channelService.saveChannel(user)).thenReturn(channel);

        mockMvc.perform(post("/channels").headers(authorize()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0]", equalTo("testNickname2")));

        verify(userService, times(1)).getUserByNick("testNickname1");
        verify(channelService, times(1)).saveChannel(user);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

    //endregion

    //region PUT("/channels/users")
    @Test
    public void shouldAddChannelOwner() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> ownersBefore = UserFixture.createListOfDefaultUser(1);
        final List<User> ownersAfter = UserFixture.createListOfDefaultUser(2);
        final List<User> membersBefore = UserFixture.createListOfDefaultUser(2, 1);
        final List<User> membersAfter = new ArrayList<>();

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

        mockMvc.perform(put("/channels/users").headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(2)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.owners[1]", equalTo("testNickname2")))
                .andExpect(jsonPath("$.members", hasSize(0)));

        verify(channelService, times(1)).getChannelById(1);
        verify(channelService, times((1))).userHasPermissionToUpdateChannel(channel, "testNickname1");
        verify(userService, times(1)).getUserByNick("testNickname2");
        verify(channelService, times(1)).updateChannel(channel, action, userToManipulate);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldRemoveChannelOwner() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> ownersBefore = UserFixture.createListOfDefaultUser(2);
        final List<User> ownersAfter = UserFixture.createListOfDefaultUser(1);
        final List<User> membersBefore = new ArrayList<>();
        final List<User> membersAfter = UserFixture.createListOfDefaultUser(2, 1);

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

        mockMvc.perform(put("/channels/users")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0]", equalTo("testNickname2")));

        verify(channelService, times(1)).getChannelById(1);
        verify(channelService, times((1))).userHasPermissionToUpdateChannel(channel, "testNickname1");
        verify(userService, times(1)).getUserByNick("testNickname2");
        verify(channelService, times(1)).updateChannel(channel, action, userToManipulate);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldAddChannelMember() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = UserFixture.createListOfDefaultUser(1);
        final List<User> membersBefore = new ArrayList<>();
        final List<User> membersAfter = UserFixture.createListOfDefaultUser(2, 1);

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

        mockMvc.perform(put("/channels/users")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0]", equalTo("testNickname2")));

        verify(channelService, times(1)).getChannelById(1);
        verify(channelService, times((1))).userHasPermissionToUpdateChannel(channel, "testNickname1");
        verify(userService, times(1)).getUserByNick("testNickname2");
        verify(channelService, times(1)).updateChannel(channel, action, userToManipulate);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldRemoveChannelMember() throws Exception {
        final var userToManipulate = UserFixture.createDefaultUser(2).build();

        final List<User> owners = UserFixture.createListOfDefaultUser(1);
        final List<User> membersBefore = UserFixture.createListOfDefaultUser(2, 1);
        final List<User> membersAfter = new ArrayList<>();

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

        mockMvc.perform(put("/channels/users")
                        .headers(authorize())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.owners", hasSize(1)))
                .andExpect(jsonPath("$.owners[0]", equalTo("testNickname1")))
                .andExpect(jsonPath("$.members", hasSize(0)));

        verify(channelService, times(1)).getChannelById(1);
        verify(channelService, times((1))).userHasPermissionToUpdateChannel(channel, "testNickname1");
        verify(userService, times(1)).getUserByNick("testNickname2");
        verify(channelService, times(1)).updateChannel(channel, action, userToManipulate);

        verifyNoMoreInteractions(channelService);
        verifyNoMoreInteractions(userService);
    }

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

        mockMvc.perform(put("/channels/users")
                        .headers(authorize())
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

        verifyNoInteractions(channelService);
        verifyNoInteractions(userService);
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

        verifyNoInteractions(channelService);
        verifyNoInteractions(userService);
    }
    //endregion

}

package com.zse.chat.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zse.chat.user.User;
import com.zse.chat.user.UserFixture;
import com.zse.chat.user.UserNotFoundException;
import com.zse.chat.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private UserService userService;

    //region GET("/messages")
    @Test
    public void shouldReturnAllMessages() throws Exception {
        List<Message> messages = new ArrayList<>();
        List<User> users = new ArrayList<>();
        users.add(UserFixture.createDefaultUser(1).build());
        users.add(UserFixture.createDefaultUser(2).build());
        messages.add(new Message(1, users.get(0), "content1", LocalDateTime.now(), null, false));
        messages.add(new Message(2, users.get(1), "content2", LocalDateTime.now(), null, false));
        messages.add(new Message(3, users.get(0), "content3", LocalDateTime.now(), null, false));
        messages.add(new Message(4, users.get(0), "content4", LocalDateTime.now(), null, false));

        when(messageService.getAllMessagesInGlobalChannel()).thenReturn(messages);

        mockMvc.perform(get("/messages"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void shouldReturnEmptyArrayOfMessages() throws Exception {
        List<Message> messages = new ArrayList<>();

        when(messageService.getAllMessagesInGlobalChannel()).thenReturn(messages);

        mockMvc.perform(get("/messages"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    //endregion

    //region GET("/messages/{id}")
    @Test
    public void shouldReturnMessageById() throws Exception {
        int id = 1;
        User user = UserFixture.createDefaultUser(1).build();
        Message message = new Message(1, user, "content1", LocalDateTime.now(), null, false);

        when(messageService.getMessageById(id)).thenReturn(message);

        mockMvc.perform(get("/messages/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(id)))
                .andExpect(jsonPath("$.authorNick", equalTo("testNickname1")))
                .andExpect(jsonPath("$.content", equalTo("content1")));
    }

    @Test
    public void shouldThrowNoMessageWithId() throws Exception {
        int id = 1;

        when(messageService.getMessageById(id)).thenThrow(new MessageNotFoundException(id));

        mockMvc.perform(get("/messages/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath(
                        "$.exceptionMessage",
                        containsString(String.valueOf(id))));
    }
    //endregion

    //region POST("/messages")
    @Test
    public void shouldCreateMessage() throws Exception {
        String nick = "testNick";
        User user = UserFixture.createDefaultUser(1).build();
        var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname(nick)
                .content("content1")
                .build();
        Message message = new Message(1, user, "content1", LocalDateTime.now(), null, false);
        String body = mapper.writeValueAsString(messageRequestDTO);

        when(userService.getUserByNick(nick)).thenReturn(user);
        when(messageService.saveMessage(messageRequestDTO, user)).thenReturn(message);

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.authorNick", equalTo("testNickname1")))
                .andExpect(jsonPath("$.content", equalTo("content1")));
    }

    @Test
    public void shouldThrowNotFoundUserWhenCreatingMessageForNonExistingUser() throws Exception {
        String nick = "testNickname1";
        var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname(nick)
                .content("content1")
                .build();

        String body = mapper.writeValueAsString(messageRequestDTO);

        when(userService.getUserByNick(nick)).thenThrow(new UserNotFoundException(nick));

        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath("$.exceptionMessage", containsString(nick)));
    }
    //endregion

    //region PUT("/messages{id}")
    @Test
    public void shouldReturnUpdatedMessage() throws Exception {
        int id = 1;
        var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname("testNickname1")
                .content("content1Updated")
                .build();
        User user = UserFixture.createDefaultUser(1).build();
        Message message = new Message(1, user, "content1Updated", LocalDateTime.now(), null, false);

        String body = mapper.writeValueAsString(messageRequestDTO);

        when(messageService.updateMessageById(id, messageRequestDTO)).thenReturn(message);

        mockMvc.perform(put("/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(id)))
                .andExpect(jsonPath("$.authorNick", equalTo("testNickname1")))
                .andExpect(jsonPath("$.content", equalTo("content1Updated")));
    }

    @Test
    public void shouldThrowMessageNotFoundWhenTryingUpdateNotExistingMessage() throws Exception {
        int id = 1;
        var messageRequestDTO = MessageController.MessageRequestDTO.builder()
                .nickname("testNickname1")
                .content("content1Updated")
                .build();

        String body = mapper.writeValueAsString(messageRequestDTO);

        when(messageService.updateMessageById(id, messageRequestDTO))
                .thenThrow(new MessageNotFoundException(id));

        mockMvc.perform(put("/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath("$.exceptionMessage", containsString(String.valueOf(id))));
    }
    //endregion

}

//package com.zse.chat.message;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.zse.chat.user.User;
//import com.zse.chat.user.UserNotFoundException;
//import com.zse.chat.user.UserService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(MessageController.class)
//class MessageControllerTest {
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private MessageService messageService;
//    @MockBean
//    private UserService userService;
//
//    @Test
//    public void shouldReturnAllMessages() throws Exception {
//        List<Message> messages = new ArrayList<>();
//        List<User> users = new ArrayList<>();
//        users.add(new User(1, "testName1", "testNick1"));
//        users.add(new User(2, "testName2", "testNick2"));
//        messages.add(new Message(1, users.get(0), "content1", LocalDateTime.now()));
//        messages.add(new Message(2, users.get(1), "content2", LocalDateTime.now()));
//        messages.add(new Message(3, users.get(0), "content3", LocalDateTime.now()));
//        messages.add(new Message(4, users.get(0), "content4", LocalDateTime.now()));
//
//        when(messageService.getAllMessages()).thenReturn(messages);
//
//        mockMvc.perform(get("/messages"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(4)));
//    }
//
//    @Test
//    public void shouldReturnEmptyArryaOfMessages() throws Exception {
//        List<Message> messages = new ArrayList<>();
//
//        when(messageService.getAllMessages()).thenReturn(messages);
//
//        mockMvc.perform(get("/messages"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(0)));
//    }
//
//    @Test
//    public void shouldReturnMessageById() throws Exception {
//        int id = 1;
//        Message message = new Message(
//                1,
//                new User(1, "testName", "testNick"),
//                "content1", LocalDateTime.now());
//
//        when(messageService.getMessageById(id)).thenReturn(message);
//
//        mockMvc.perform(get("/messages/1"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", equalTo(id)))
//                .andExpect(jsonPath("$.authorNick", equalTo("testNick")))
//                .andExpect(jsonPath("$.content", equalTo("content1")));
//    }
//
//    @Test
//    public void shouldThrowNoMessageWithId() throws Exception {
//        int id = 1;
//
//        when(messageService.getMessageById(id)).thenThrow(new MessageNotFoundException(id));
//
//        mockMvc.perform(get("/messages/1"))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.responseCode", equalTo(404)))
//                .andExpect(jsonPath(
//                        "$.exceptionMessage",
//                        containsString(String.valueOf(id))));
//    }
//
//    @Test
//    public void shouldCreateMessage() throws Exception {
//        String nick = "testNick";
//        User user = new User(1, "testName", "testNick");
//        var messageRequestDTO = MessageController.MessageRequestDTO.builder()
//                .authorNick(nick)
//                .content("content1")
//                .build();
//        Message message = new Message(1, user, "content1", LocalDateTime.now());
//        String body = mapper.writeValueAsString(messageRequestDTO);
//
//        when(userService.getUserByNick(nick)).thenReturn(user);
//        when(messageService.sendMessage(messageRequestDTO, user)).thenReturn(message);
//
//        mockMvc.perform(post("/messages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", equalTo(1)))
//                .andExpect(jsonPath("$.authorNick", equalTo("testNick")))
//                .andExpect(jsonPath("$.content", equalTo("content1")));
//    }
//
//    @Test
//    public void shouldThrowNotFoundUserWhenCreatingMessageForNonExistingUser() throws Exception {
//        String nick = "testNick";
//        var messageRequestDTO = MessageController.MessageRequestDTO.builder()
//                .authorNick(nick)
//                .content("content1")
//                .build();
//
//        String body = mapper.writeValueAsString(messageRequestDTO);
//
//        when(userService.getUserByNick(nick)).thenThrow(new UserNotFoundException(nick));
//
//        mockMvc.perform(post("/messages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.responseCode", equalTo(404)))
//                .andExpect(jsonPath(
//                        "$.exceptionMessage",
//                        containsString(nick)));
//    }
//
//    @Test
//    public void shouldReturnUpdatedMessage() throws Exception {
//        int id = 1;
//        var messageRequesDTO = MessageController.MessageRequestDTO.builder()
//                .authorNick("nickTest")
//                .content("content1Updated")
//                .build();
//        Message message = new Message(
//                1,
//                new User(1, "testName", "nickTest"),
//                "content1Updated", LocalDateTime.now());
//
//        String body = mapper.writeValueAsString(messageRequesDTO);
//
//        when(messageService.updateMessageById(id, messageRequesDTO)).thenReturn(message);
//
//        mockMvc.perform(put("/messages/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", equalTo(id)))
//                .andExpect(jsonPath("$.authorNick", equalTo("nickTest")))
//                .andExpect(jsonPath("$.content", equalTo("content1Updated")));
//    }
//
//    @Test
//    public void shouldThrowMessageNotFoundWhenTryingUpdateNotExistingMessage() throws Exception {
//        int id = 1;
//        var messageRequesDTO = MessageController.MessageRequestDTO.builder()
//                .authorNick("nickTest")
//                .content("content1Updated")
//                .build();
//
//        String body = mapper.writeValueAsString(messageRequesDTO);
//
//        when(messageService.updateMessageById(id, messageRequesDTO)).thenThrow(new MessageNotFoundException(id));
//
//        mockMvc.perform(put("/messages/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.responseCode", equalTo(404)))
//                .andExpect(jsonPath(
//                        "$.exceptionMessage",
//                        containsString(String.valueOf(id))));
//    }
//
//}

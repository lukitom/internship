package com.zse.chat.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void shouldReturnAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "testName1", "testNick1"));
        users.add(new User(2, "testName2", "testNick2"));
        users.add(new User(3, "testName3", "testNick3"));
        users.add(new User(4, "testName4", "testNick4"));

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void shouldReturnEmptyArrayOfUsers() throws Exception {
        List<User> users = new ArrayList<>();

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldReturnUserWithNick() throws Exception {
        String nick = "testNick";
        User user = new User(1, "testName", nick);

        when(userService.getUserById(nick)).thenReturn(user);

        mockMvc.perform(get("/users/testNick"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("testName")))
                .andExpect(jsonPath("$.nick", equalTo(nick)));
    }

    @Test
    public void shouldThrowNotExistingUser() throws Exception {
        String nick = "testNick";

        when(userService.getUserById(nick)).thenThrow(new UserNotFoundException(nick));

        mockMvc.perform(get("/users/testNick"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath(
                        "$.exceptionMessage",
                        containsString(nick)));
    }

    @Test
    public void shouldCreateUser() throws Exception {
        UserController.UserDTO userDTO = UserController.UserDTO.builder()
                .name("testName")
                .nick("testNick")
                .build();
        User user = new User(1, "testName", "testNick");
        String body = mapper.writeValueAsString(userDTO);

        when(userService.saveUser(userDTO)).thenReturn(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("testName")))
                .andExpect(jsonPath("$.nick", equalTo("testNick")));
    }

    @Test
    public void shouldUpdateUserNameFoundByNick() throws Exception {
        String nick = "testNick";
        UserController.UserDTO userDTO = UserController.UserDTO.builder()
                .name("testNameUpdated")
                .nick(nick)
                .build();
        User user = new User(1, "testNameUpdated", nick);
        String body = mapper.writeValueAsString(userDTO);

        when(userService.updateUserName(nick, userDTO)).thenReturn(user);


        mockMvc.perform(put("/users/testNick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("testNameUpdated")))
                .andExpect(jsonPath("$.nick", equalTo(nick)));
    }

    @Test
    public void shouldThrowUserNotFoundTryingUpdateNotExistingUser() throws Exception {
        String nick = "testNick";
        UserController.UserDTO userDTO = UserController.UserDTO.builder()
                .name("testNameUpdated")
                .nick(nick)
                .build();
        String body = mapper.writeValueAsString(userDTO);

        when(userService.updateUserName(nick, userDTO)).thenThrow(new UserNotFoundException(nick));

        mockMvc.perform(put("/users/testNick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath(
                        "$.exceptionMessage",
                        containsString(nick)));
    }

}

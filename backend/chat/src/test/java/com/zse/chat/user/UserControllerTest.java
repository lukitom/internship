package com.zse.chat.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeAll
    static void configureMapper(){
        mapper.registerModule(new Jdk8Module());
    }

    private User.UserBuilder createUserForTest(int number){
        return User.builder()
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number)
                .country("testCountry" + number)
                .city("testCity" + number)
                .phoneNumber("testPhoneNumber" + number)

                .userStatus(UserStatus.OFFLINE)
                .userLanguage(Language.POLISH)
                .contentLanguage(ContentLanguage.MY_LANGUAGE)
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))

                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .deleted(false);
    }

    private UserController.UpdateUserDTO.UpdateUserDTOBuilder createUserForTestUpdate(int number){
        return UserController.UpdateUserDTO.builder()
                .nickname("testNickname" + number)
                .firstName(Optional.of("testFirstName" + number))
                .lastName(Optional.of("testLastName" + number))
                .phoneNumber(Optional.of("testPhoneNumber" + number))
                .country(Optional.of("testCountry" + number))
                .city(Optional.of("testCity" + number))
                .language(Optional.of(Language.POLISH))
                .contentLanguage(Optional.of(ContentLanguage.MY_LANGUAGE))

                .showFirstNameAndLastName(Optional.of(false))
                .showEmail(Optional.of(false))
                .showPhoneNumber(Optional.of(false))
                .showAddress(Optional.of(false));
    }

    @Test
    public void shouldReturnAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(createUserForTest(1).build());
        users.add(createUserForTest(2).build());
        users.add(createUserForTest(3).build());
        users.add(createUserForTest(4).build());

        when(userService.getAllUsers()).thenReturn(users);

        for (User user : users){
            System.out.println(user.getNickname());
        }

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
    public void shouldReturnUserByIdVisibleNickname() throws Exception {
        String nick = "testNickname1";
        User user = createUserForTest(1).build();

        when(userService.getUserByNick(nick)).thenReturn(user);

        mockMvc.perform(get("/users/testNickname1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo(nick)));
    }

    @Test
    public void shouldThrowNotExistingUser() throws Exception {
        String nick = "testNick";

        when(userService.getUserByNick(nick)).thenThrow(new UserNotFoundException(nick));

        mockMvc.perform(get("/users/" + nick))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath(
                        "$.exceptionMessage",
                        containsString(nick)));
    }

    @Test
    public void shouldCreateUser() throws Exception {
        UserController.CreateUserDTO createUserDTO = UserController.CreateUserDTO.builder()
                .nickname("testNickname1")
                .firstName("testFirstName1")
                .lastName("testLastName1")
                .email("testEmail1")
                .phoneNumber("testPhoneNumber1")
                .country("testCountry1")
                .city("testCity1")
                .language(Optional.of(Language.POLISH))
                .contentLanguage(Optional.of(ContentLanguage.MY_LANGUAGE))
                .build();
        User user = createUserForTest(1).build();
        String body = mapper.writeValueAsString(createUserDTO);

        when(userService.saveUser(ArgumentMatchers.any())).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo("testNickname1")));
    }

    @Test
    public void shouldUpdateUserDetailsFoundByNicknameVisibleNickname() throws Exception {
        UserController.UpdateUserDTO updateUserDTO = createUserForTestUpdate(1).build();
        User user = createUserForTest(1).build();
        String body = mapper.writeValueAsString(updateUserDTO);

        when(userService.updateUser(ArgumentMatchers.any())).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo("testNickname1")));
    }

    @Test
    //TODO: check if any of the rest fields are visible
    public void shouldUpdateUserDetailsFoundByNicknameVisibleEmail() throws Exception {
        UserController.UpdateUserDTO updateUserDTO = createUserForTestUpdate(1)
                .showEmail(Optional.of(true)).build();
        User user = createUserForTest(1)
                .showEmail(true).build();
        String body = mapper.writeValueAsString(updateUserDTO);

        when(userService.updateUser(ArgumentMatchers.any())).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo("testNickname1")))
                .andExpect(jsonPath("$.email", equalTo("testEmail1")));
    }

    @Test
    //TODO: check if any of the rest fields are visible
    public void shouldUpdateUserDetailsFoundByNicknameVisiblePhoneNumber() throws Exception {
        UserController.UpdateUserDTO updateUserDTO = createUserForTestUpdate(1)
                .showPhoneNumber(Optional.of(true)).build();
        User user = createUserForTest(1)
                .showPhoneNumber(true).build();
        String body = mapper.writeValueAsString(updateUserDTO);

        when(userService.updateUser(ArgumentMatchers.any())).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo("testNickname1")))
                .andExpect(jsonPath("$.phoneNumber", equalTo("testPhoneNumber1")));
    }

//    @Test
//    public void shouldThrowUserNotFoundTryingUpdateNotExistingUser() throws Exception {
//        String nick = "testNick";
//        UserController.UserDTO userDTO = UserController.UserDTO.builder()
//                .name("testNameUpdated")
//                .nick(nick)
//                .build();
//        String body = mapper.writeValueAsString(userDTO);
//
//        when(userService.updateUserName(nick, userDTO)).thenThrow(new UserNotFoundException(nick));
//
//        mockMvc.perform(put("/users/testNick")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.responseCode", equalTo(404)))
//                .andExpect(jsonPath(
//                        "$.exceptionMessage",
//                        containsString(nick)));
//    }

}

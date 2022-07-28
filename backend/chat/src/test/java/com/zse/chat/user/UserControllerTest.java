package com.zse.chat.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    //region fixture
    private User.UserBuilder createUserForTest(int number){
        return User.builder()
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number)
                .country("testCountry" + number)
                .city("testCity" + number)
                .phoneNumber("testPhoneNumber" + number)

                .userStatus(User.UserStatus.OFFLINE)
                .userLanguage(User.Language.POLISH)
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))

                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .deleted(false);
    }

    private UserController.CreateUserDTO.CreateUserDTOBuilder createUserForTestCreate(int number) {
        return UserController.CreateUserDTO.builder()
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number + "@mail.com")
                .phoneNumber("11122233" + number)
                .country("Testcountry")
                .city("Testcity")
                .language(Optional.of(User.Language.POLISH));
    }

    private UserController.UpdateUserDTO.UpdateUserDTOBuilder createUserForTestUpdate(int number){
        return UserController.UpdateUserDTO.builder()
                .nickname("testNickname" + number)
                .firstName(Optional.of("testFirstName" + number))
                .lastName(Optional.of("testLastName" + number))
                .phoneNumber(Optional.of("testPhoneNumber" + number))
                .country(Optional.of("testCountry" + number))
                .city(Optional.of("testCity" + number))
                .userStatus(Optional.of(User.UserStatus.OFFLINE))
                .language(Optional.of(User.Language.POLISH))
                .timeZone(Optional.of("Europe/Warsaw"))

                .showFirstNameAndLastName(Optional.of(false))
                .showEmail(Optional.of(false))
                .showPhoneNumber(Optional.of(false))
                .showAddress(Optional.of(false))

                .deleted(Optional.of(false));
    }
    //endregion

    //region GET("/users")
    @Test
    public void shouldReturnAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(createUserForTest(1).build());
        users.add(createUserForTest(2).build());
        users.add(createUserForTest(3).build());
        users.add(createUserForTest(4).build());

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
    //endregion

    //region GET("/users/{nickname}")
    @Test
    public void shouldReturnUserByNicknameVisibleNickname() throws Exception {
        String nick = "testNickname1";
        User user = createUserForTest(1).build();

        when(userService.getUserByNick(nick)).thenReturn(user);

        mockMvc.perform(get("/users/testNickname1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo(nick)));
    }

    @Test
    public void shouldThrowNotExistingUserWhenTryToFindByNotExistingNick() throws Exception {
        String nickname = "testNickname1";

        when(userService.getUserByNick(nickname)).thenThrow(new UserNotFoundException(nickname));

        mockMvc.perform(get("/users/" + nickname))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath("$.exceptionMessage", containsString(nickname)));
    }
    //endregion

    //region POST("/users")
    @Test
    public void shouldCreateUser() throws Exception {
        UserController.CreateUserDTO createUserDTO = createUserForTestCreate(1).build();
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

    @ParameterizedTest(name = "missingNickname: {0}, missingEmail: {1}")
    @CsvSource({"false, true", "true, false"})
    public void shouldThrowMissingArgumentTryingCreateUserWithMissingArgument(
            boolean missingNickname,
            boolean missingEmail
    ) throws Exception {
        UserController.CreateUserDTO createUserDTO = createUserForTestCreate(1)
                .nickname(missingNickname ? null : "testNickname1")
                .nickname(missingEmail? null : "testEmail1")
                .build();
        String body = mapper.writeValueAsString(createUserDTO);

        when(userService.saveUser(ArgumentMatchers.any())).thenThrow(new MissingPayloadFieldException(
                missingNickname ? "nickname": missingEmail ? "email" : null
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode", equalTo(400)))
                .andExpect(jsonPath("$.exceptionMessage", containsString(
                        missingNickname ? "nickname" : missingEmail ? "email" : null
                )));
    }

    @Test
    public void shouldThrowUserWithNickExists() throws Exception {
        UserController.CreateUserDTO createUserDTO = createUserForTestCreate(1).build();
        String body = mapper.writeValueAsString(createUserDTO);

        when(userService.saveUser(ArgumentMatchers.any()))
                .thenThrow(new UserWithNickAlreadyExistsException("testNickname1"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode", equalTo(400)))
                .andExpect(jsonPath("$.exceptionMessage", containsString("testNickname1")));
    }

    @Test
    public void shouldThrowUserWithEmailExists() throws Exception {
        UserController.CreateUserDTO createUserDTO = createUserForTestCreate(1).build();
        String body = mapper.writeValueAsString(createUserDTO);

        when(userService.saveUser(ArgumentMatchers.any()))
                .thenThrow(new UserWithEmailAlreadyExistsExeption("testEmail1"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode", equalTo(400)))
                .andExpect(jsonPath("$.exceptionMessage", containsString("testEmail1")));
    }
    //endregion

    //region PUT("/users")
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

    @ParameterizedTest(name = "name: {0}, email: {1}, phone: {2}, address: {3}")
    @CsvFileSource(resources = "/visibilityUserFields.csv", numLinesToSkip = 1)
    public void shouldUpdateUserDetailsFoundByNicknameVisible(
            boolean showFirstNameAndLastName,
            boolean showEmail,
            boolean showPhoneNumber,
            boolean showAddress) throws Exception {
        UserController.UpdateUserDTO updateUserDTO = createUserForTestUpdate(1)
                .showFirstNameAndLastName(Optional.of(showFirstNameAndLastName))
                .showEmail(Optional.of(showEmail))
                .showPhoneNumber(Optional.of(showPhoneNumber))
                .showAddress(Optional.of(showAddress))
                .build();
        User user = createUserForTest(1)
                .showFirstNameAndLastName(showFirstNameAndLastName)
                .showEmail(showEmail)
                .showPhoneNumber(showPhoneNumber)
                .showAddress(showAddress)
                .build();
        String body = mapper.writeValueAsString(updateUserDTO);

        when(userService.updateUser(ArgumentMatchers.any())).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", equalTo("testNickname1")))
                .andExpect(
                        showFirstNameAndLastName ?
                                jsonPath("$.firstName", equalTo("testFirstName1")) :
                                jsonPath("$.firstName").doesNotExist()
                )
                .andExpect(
                        showFirstNameAndLastName ?
                                jsonPath("$.lastName", equalTo("testLastName1")) :
                                jsonPath("$.lastName").doesNotExist()
                )
                .andExpect(
                        showEmail ?
                                jsonPath("$.email", equalTo("testEmail1")) :
                                jsonPath("$.email").doesNotExist()
                )
                .andExpect(
                        showPhoneNumber ?
                                jsonPath("$.phoneNumber", equalTo("testPhoneNumber1")) :
                                jsonPath("$.phoneNumber").doesNotExist()
                )
                .andExpect(
                        showAddress ?
                                jsonPath("$.country", equalTo("testCountry1")) :
                                jsonPath("$.country").doesNotExist()
                )
                .andExpect(
                        showAddress ?
                                jsonPath("$.city", equalTo("testCity1")) :
                                jsonPath("$.city").doesNotExist()
                );
    }

    @Test
    public void shouldThrowMissingArgumentTryingUpdateUserWithoutGivenNickname() throws Exception {
        UserController.UpdateUserDTO updateUserDTO = createUserForTestUpdate(1)
                .nickname(null).build();
        String body = mapper.writeValueAsString(updateUserDTO);

        when(userService.updateUser(ArgumentMatchers.any()))
                .thenThrow(new MissingPayloadFieldException("nickname"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode", equalTo(400)))
                .andExpect(jsonPath("$.exceptionMessage", containsString("nickname")));
    }

    @Test
    public void shouldThrowUserNotFoundTryingUpdateNotExistingUser() throws Exception {
        String nickname = "testNickname";
        UserController.UpdateUserDTO updateUserDTO = createUserForTestUpdate(1).build();
        String body = mapper.writeValueAsString(updateUserDTO);

        when(userService.updateUser(ArgumentMatchers.any())).thenThrow(new UserNotFoundException(nickname));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath("$.exceptionMessage", containsString(nickname)));
    }
    //endregion

}

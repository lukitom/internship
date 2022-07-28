package com.zse.chat.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    //region fixture
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    private UserController.CreateUserDTO.CreateUserDTOBuilder createUserDTOForTestCreate(int number){
        return UserController.CreateUserDTO.builder()
                .nickname("testNickname" + number)
                .firstName("testFirstName" + number)
                .lastName("testLastName" + number)
                .email("testEmail" + number)
                .phoneNumber("testPhoneNumber" + number)
                .country("testCountry" + number)
                .city("testCity" + number)
                .language(Optional.of(User.Language.POLISH));
    }

    private UserController.UpdateUserDTO.UpdateUserDTOBuilder createUserDTOForTestUpdate(int number) {
        return UserController.UpdateUserDTO.builder()
                .nickname("testNickname" + number)
                .firstName(Optional.of("testFirstNameUpdated" + number))
                .lastName(Optional.of("testLastNameUpdated" + number))
                .phoneNumber(Optional.of("testPhoneNumberUpdated" + number))
                .country(Optional.of("testCountryUpdated" + number))
                .city(Optional.of("testCityUpdated" + number))
                .userStatus(Optional.of(User.UserStatus.OFFLINE))
                .language(Optional.of(User.Language.POLISH))
                .timeZone(Optional.of("Europe/Warsaw"))
                .showFirstNameAndLastName(Optional.of(true))
                .showEmail(Optional.of(true))
                .showPhoneNumber(Optional.of(true))
                .showAddress(Optional.of(true))
                .deleted(Optional.of(false));
    }
    //endregion

    //region saveUser()
    @Test
    public void shouldSaveUserCorrectly(){
        UserController.CreateUserDTO createUserDTO = createUserDTOForTestCreate(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail1")).thenReturn(Optional.empty());
        when(userRepository.save(ArgumentMatchers.any(User.class))).then(AdditionalAnswers.returnsFirstArg());

        User returnedUser = userService.saveUser(createUserDTO);

        assertThat(returnedUser, notNullValue());

        verify(userRepository, times(1)).findByNickname("testNickname1");
        verify(userRepository, times(1)).findByEmail("testEmail1");
        verify(userRepository, times(1)).save(ArgumentMatchers.any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void validateCreatedUser(){
        UserController.CreateUserDTO createUserDTO = createUserDTOForTestCreate(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail1")).thenReturn(Optional.empty());
        when(userRepository.save(ArgumentMatchers.any(User.class))).then(AdditionalAnswers.returnsFirstArg());

        User user = userService.saveUser(createUserDTO);

        assertThat(user.getNickname(), equalTo("testNickname1"));
        assertThat(user.getFirstName(), equalTo("testFirstName1"));
        assertThat(user.getLastName(), equalTo("testLastName1"));
        assertThat(user.getEmail(), equalTo("testEmail1"));
        assertThat(user.getPhoneNumber(), equalTo("testPhoneNumber1"));
        assertThat(user.getCountry(), equalTo("testCountry1"));
        assertThat(user.getCity(), equalTo("testCity1"));
        assertThat(user.getUserLanguage(), equalTo(User.Language.POLISH));
        assertThat(user.getTimeZone(), equalTo(TimeZone.getTimeZone("Europe/Warsaw")));
        assertThat(user.getUserStatus(), equalTo(User.UserStatus.OFFLINE));
        assertThat(user.getShowFirstNameAndLastName(), equalTo(false));
        assertThat(user.getShowEmail(), equalTo(false));
        assertThat(user.getShowPhoneNumber(), equalTo(false));
        assertThat(user.getShowAddress(), equalTo(false));
        assertThat(user.getDeleted(), equalTo(false));

        verify(userRepository, times(1))
                .findByNickname("testNickname1");
        verify(userRepository, times(1))
                .findByEmail("testEmail1");
        verify(userRepository, times(1))
                .save(ArgumentMatchers.any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @ParameterizedTest(name = "missingNickname: {0}, missingEmail: {1}")
    @CsvSource({"false, true","true, false"})
    public void shouldThrowMissingArgumentWhenTryingSaveUserWithIncompleteData(
            boolean missingNickname,
            boolean missingEmail) {
        UserController.CreateUserDTO createUserDTO = createUserDTOForTestCreate(1)
                .nickname(missingNickname ? null : "testNickname1")
                .email(missingEmail ? null : "testEmail1")
                .build();

        assertThrows(
                MissingPayloadFieldException.class,
                () -> userService.saveUser(createUserDTO));

        try {
            userService.saveUser(createUserDTO);
        } catch (MissingPayloadFieldException e) {
            assertThat(e.getMessage(), containsString(
                    missingNickname ? "nickname" : missingEmail ? "email" : null)
            );
        }
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldThrowUserExistWithNickname(){
        UserController.CreateUserDTO createUserDTO = createUserDTOForTestCreate(1).build();
        User user = UserFixture.createDefaultUser(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.of(user));

        assertThrows(UserWithNickAlreadyExistsException.class,
                () -> userService.saveUser(createUserDTO));

        verify(userRepository, times(1)).findByNickname("testNickname1");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldThrowUserExistWithEmail(){
        UserController.CreateUserDTO createUserDTO = createUserDTOForTestCreate(1).build();
        User user = UserFixture.createDefaultUser(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail1")).thenReturn(Optional.of(user));

        assertThrows(UserWithEmailAlreadyExistsExeption.class,
                () -> userService.saveUser(createUserDTO));

        verify(userRepository, times(1)).findByNickname("testNickname1");
        verify(userRepository, times(1)).findByEmail("testEmail1");
        verifyNoMoreInteractions(userRepository);
    }
    //endregion

    //region getAllUsers()
    @Test
    public void shouldReturnListOfUsers(){
        List<User> users = new ArrayList<>();
        users.add(UserFixture.createDefaultUser(1).build());
        users.add(UserFixture.createDefaultUser(2).build());
        users.add(UserFixture.createDefaultUser(3).build());
        users.add(UserFixture.createDefaultUser(4).build());

        when(userRepository.findAll()).thenReturn(users);

        List<User> returned = userService.getAllUsers();

        assertThat(returned, hasSize(4));
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldReturnEmptyListOfUsers(){
        List<User> users = new ArrayList<>();

        when(userRepository.findAll()).thenReturn(users);

        List<User> returned = userService.getAllUsers();

        assertThat(returned, hasSize(0));

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }
    //endregion

    //region getUserByNick()
    @Test
    public void shouldReturnUserByNick(){
        User user = UserFixture.createDefaultUser(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.of(user));

        User returnedUser = userService.getUserByNick("testNickname1");

        assertThat(returnedUser, notNullValue());

        verify(userRepository, times(1)).findByNickname("testNickname1");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void validateGetUserByNickname(){
        UserController.CreateUserDTO createUserDTO = createUserDTOForTestCreate(1).build();
        User user = UserFixture.createDefaultUser(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.of(user));

        User returnedUser = userService.getUserByNick("testNickname1");

        assertThat(returnedUser.getNickname(), equalTo("testNickname1"));
        assertThat(returnedUser.getFirstName(), equalTo("testFirstName1"));
        assertThat(returnedUser.getLastName(), equalTo("testLastName1"));
        assertThat(returnedUser.getEmail(), equalTo("testEmail1@mail.com"));
        assertThat(returnedUser.getPhoneNumber(), equalTo("111222331"));
        assertThat(returnedUser.getCountry(), equalTo("Testcountry"));
        assertThat(returnedUser.getCity(), equalTo("Testcity"));
        assertThat(returnedUser.getUserLanguage(), equalTo(User.Language.POLISH));
        assertThat(returnedUser.getTimeZone(), equalTo(TimeZone.getTimeZone("Europe/Warsaw")));
        assertThat(returnedUser.getUserStatus(), equalTo(User.UserStatus.OFFLINE));
        assertThat(returnedUser.getShowFirstNameAndLastName(), equalTo(false));
        assertThat(returnedUser.getShowEmail(), equalTo(false));
        assertThat(returnedUser.getShowPhoneNumber(), equalTo(false));
        assertThat(returnedUser.getShowAddress(), equalTo(false));
        assertThat(returnedUser.getDeleted(), equalTo(false));

        verify(userRepository, times(1))
                .findByNickname("testNickname1");
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    public void shouldThrowUserNotFoundWhenTryingToFindByNotExistingNickname(){
        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByNick("testNickname1"));

        verify(userRepository, times(1))
                .findByNickname("testNickname1");
        verifyNoMoreInteractions(userRepository);
    }
    //endregion

    //region updateUser()
    @Test
    public void shouldUpdateUser(){
        UserController.UpdateUserDTO updateUserDTO = createUserDTOForTestUpdate(1).build();
        User user = UserFixture.createDefaultUser(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.of(user));
        when(userRepository.save(ArgumentMatchers.any(User.class))).then(AdditionalAnswers.returnsFirstArg());

        User updatedUser = userService.updateUser(updateUserDTO);

        assertThat(updatedUser, notNullValue());

        verify(userRepository, times(1)).findByNickname("testNickname1");
        verify(userRepository, times(1)).save(ArgumentMatchers.any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void validateReturnedUserFields(){
        UserController.UpdateUserDTO updateUserDTO = createUserDTOForTestUpdate(1).build();
        User user = UserFixture.createDefaultUser(1).build();

        when(userRepository.findByNickname("testNickname1")).thenReturn(Optional.of(user));
        when(userRepository.save(ArgumentMatchers.any(User.class))).then(AdditionalAnswers.returnsFirstArg());

        User updatedUser = userService.updateUser(updateUserDTO);

        assertThat(updatedUser.getNickname(), equalTo("testNickname1"));
        assertThat(updatedUser.getFirstName(), equalTo("testFirstNameUpdated1"));
        assertThat(updatedUser.getLastName(), equalTo("testLastNameUpdated1"));
        assertThat(updatedUser.getEmail(), equalTo("testEmail1@mail.com"));
        assertThat(updatedUser.getPhoneNumber(), equalTo("testPhoneNumberUpdated1"));
        assertThat(updatedUser.getCountry(), equalTo("testCountryUpdated1"));
        assertThat(updatedUser.getCity(), equalTo("testCityUpdated1"));
        assertThat(updatedUser.getUserLanguage(), equalTo(User.Language.POLISH));
        assertThat(updatedUser.getUserStatus(), equalTo(User.UserStatus.OFFLINE));
        assertThat(updatedUser.getShowFirstNameAndLastName(), equalTo(true));
        assertThat(updatedUser.getShowEmail(), equalTo(true));
        assertThat(updatedUser.getShowPhoneNumber(), equalTo(true));
        assertThat(updatedUser.getShowAddress(), equalTo(true));
        assertThat(updatedUser.getDeleted(), equalTo(false));

        verify(userRepository, times(1))
                .findByNickname("testNickname1");
        verify(userRepository, times(1))
                .save(ArgumentMatchers.any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldThrowMissingArgumentNickname(){
        UserController.UpdateUserDTO updateUserDTO = createUserDTOForTestUpdate(1)
                .nickname(null)
                .build();

        assertThrows(MissingPayloadFieldException.class, () -> userService.updateUser(updateUserDTO));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void shouldThrowUserNotFoundTryingUpdateNotExistingUser(){
        UserController.UpdateUserDTO updateUserDTO = createUserDTOForTestUpdate(1).build();

        when(userRepository.findByNickname("testNickname1"))
                .thenThrow(new UserNotFoundException("testNickname1"));

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(updateUserDTO));

        verify(userRepository, times(1))
                .findByNickname("testNickname1");
        verifyNoMoreInteractions(userRepository);
    }
    //endregion

}
